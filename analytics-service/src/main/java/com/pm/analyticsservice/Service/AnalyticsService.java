package com.pm.analyticsservice.Service;

import com.pm.analyticsservice.DTO.*;
import com.pm.analyticsservice.Repository.paymentEventRepo;
import com.pm.analyticsservice.model.PaymentEventRecord;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final paymentEventRepo repository;

    // MongoTemplate provides the aggregation pipeline API.
    // MongoRepository only handles simple CRUD and derived queries — for GROUP BY / SUM / sort-and-limit
    // we need the lower-level template, which maps directly to MongoDB's aggregation framework.
    private final MongoTemplate mongoTemplate;

    // ── Kafka-triggered method ────────────────────────────────────────────────

    public void recordEvent(PaymentEventDTO event) {
        if (repository.existsByPaymentId(event.getPaymentId())) {
            log.warn("Duplicate analytics event for paymentId={} — skipping", event.getPaymentId());
            return;
        }

        PaymentEventRecord record = new PaymentEventRecord();
        record.setPaymentId(event.getPaymentId());
        record.setFromAccountId(event.getFromAccountId());
        record.setToAccountId(event.getToAccountId());
        record.setAmount(new BigDecimal(event.getAmount()));
        record.setStatus(event.getStatus());
        record.setType(event.getType());
        // Parse the ISO-8601 string from the event back to LocalDateTime for DB storage
        record.setOccurredAt(LocalDateTime.parse(event.getOccurredAt()));
        record.setRecordedAt(LocalDateTime.now());

        repository.save(record);
        log.info("Recorded analytics event: paymentId={} status={} amount={}",
                event.getPaymentId(), event.getStatus(), event.getAmount());
    }

    // ── REST-accessible analytics methods ────────────────────────────────────

    public AnalyticsSummaryDTO getSummary() {
        long total     = repository.count();
        long completed = repository.countByStatus("COMPLETED");
        long failed    = repository.countByStatus("FAILED");

        // Aggregation pipeline: no $match (whole collection), $group with $sum on "amount".
        // Returns a single document: { "_id": null, "total": <BigDecimal> }
        Aggregation sumAgg = Aggregation.newAggregation(
                Aggregation.group().sum("amount").as("total"));
        AggregationResults<Document> sumResult =
                mongoTemplate.aggregate(sumAgg, PaymentEventRecord.class, Document.class);
        Document sumDoc = sumResult.getUniqueMappedResult();

        // Guard against empty collection — MongoDB returns no result doc if there are 0 records
        BigDecimal totalVolume = (sumDoc != null && sumDoc.get("total") != null)
                ? new BigDecimal(sumDoc.get("total").toString())
                : BigDecimal.ZERO;

        double successRate = total > 0 ? (completed * 100.0 / total) : 0.0;

        AnalyticsSummaryDTO dto = new AnalyticsSummaryDTO();
        dto.setTotalPayments(total);
        dto.setCompletedPayments(completed);
        dto.setFailedPayments(failed);
        dto.setTotalVolume(totalVolume.setScale(2, RoundingMode.HALF_UP).toPlainString());
        dto.setSuccessRate(String.format("%.1f%%", successRate));
        return dto;
    }

    public List<DailyVolumeDTO> getDailyVolume(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date must be before or equal to 'to' date");
        }

        // Bump 'to' by one day so the range is fully inclusive of the end date
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt   = to.plusDays(1).atStartOfDay();

        // Pipeline:
        // 1. $match  — filter to the requested date range
        // 2. $project — extract "YYYY-MM-DD" string from the occurredAt Date field
        // 3. $group  — count and sum amount per day string
        // 4. $sort   — ascending by day so the client gets a timeline
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("occurredAt").gte(fromDt).lt(toDt)),
                Aggregation.project("amount")
                        .and(DateOperators.dateOf("occurredAt").toString("%Y-%m-%d")).as("day"),
                Aggregation.group("day")
                        .count().as("count")
                        .sum("amount").as("totalAmount"),
                Aggregation.sort(Sort.Direction.ASC, "_id"));

        AggregationResults<Document> results =
                mongoTemplate.aggregate(agg, PaymentEventRecord.class, Document.class);

        // Each result doc: { "_id": "2025-01-15", "count": 4, "totalAmount": 2500.00 }
        return results.getMappedResults().stream()
                .map(doc -> new DailyVolumeDTO(
                        doc.getString("_id"),
                        ((Number) doc.get("count")).longValue(),
                        new BigDecimal(doc.get("totalAmount").toString())
                                .setScale(2, RoundingMode.HALF_UP).toPlainString()))
                .toList();
    }

    public List<TopAccountDTO> getTopAccounts(int limit) {
        if (limit <= 0) limit = 10;

        // Two pipelines: one for sent volume, one for received volume.
        // Merged in Java so every account's total reflects both directions.
        Aggregation sentAgg = Aggregation.newAggregation(
                Aggregation.group("fromAccountId").sum("amount").as("volume"));
        Aggregation receivedAgg = Aggregation.newAggregation(
                Aggregation.group("toAccountId").sum("amount").as("volume"));

        Map<String, BigDecimal> volumeByAccount = new HashMap<>();

        // Add sent volumes
        for (Document doc : mongoTemplate.aggregate(sentAgg, PaymentEventRecord.class, Document.class).getMappedResults()) {
            String accountId = doc.getString("_id");
            BigDecimal amount = new BigDecimal(doc.get("volume").toString());
            volumeByAccount.merge(accountId, amount, BigDecimal::add);
        }

        // Add received volumes
        for (Document doc : mongoTemplate.aggregate(receivedAgg, PaymentEventRecord.class, Document.class).getMappedResults()) {
            String accountId = doc.getString("_id");
            BigDecimal amount = new BigDecimal(doc.get("volume").toString());
            volumeByAccount.merge(accountId, amount, BigDecimal::add);
        }

        // Sort descending by total volume and take top N
        int cap = limit;
        return volumeByAccount.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(cap)
                .map(e -> new TopAccountDTO(
                        e.getKey(),
                        e.getValue().setScale(2, RoundingMode.HALF_UP).toPlainString()))
                .toList();
    }
}
