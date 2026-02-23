package com.pm.analyticsservice.Service;

import com.pm.analyticsservice.DTO.*;
import com.pm.analyticsservice.Repository.paymentEventRepo;
import com.pm.analyticsservice.model.PaymentEventRecord;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private paymentEventRepo repository;
    @Mock private MongoTemplate mongoTemplate;

    @InjectMocks private AnalyticsService analyticsService;

    // ── recordEvent ──────────────────────────────────────────────────────────

    @Test
    void recordEvent_shouldSaveRecord_whenPaymentIsNew() {
        PaymentEventDTO event = buildEvent("PAY-001", "COMPLETED", "1000.00");
        when(repository.existsByPaymentId("PAY-001")).thenReturn(false);

        analyticsService.recordEvent(event);

        verify(repository).save(any(PaymentEventRecord.class));
    }

    @Test
    void recordEvent_shouldSkip_whenDuplicatePaymentId() {
        PaymentEventDTO event = buildEvent("PAY-001", "COMPLETED", "1000.00");
        when(repository.existsByPaymentId("PAY-001")).thenReturn(true);

        analyticsService.recordEvent(event);

        verify(repository, never()).save(any());
    }

    // ── getSummary ───────────────────────────────────────────────────────────

    @Test
    void getSummary_shouldReturnAggregatedStats() {
        when(repository.count()).thenReturn(10L);
        when(repository.countByStatus("COMPLETED")).thenReturn(8L);
        when(repository.countByStatus("FAILED")).thenReturn(2L);

        // Build the mock before passing it to thenReturn() —
        // Mockito does not allow nested when() calls inside thenReturn() arguments
        Document totalDoc = new Document("total", "5000.00");
        AggregationResults<Document> sumResult = buildMockResults(List.of(totalDoc));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq(PaymentEventRecord.class), eq(Document.class)))
                .thenReturn(sumResult);

        AnalyticsSummaryDTO dto = analyticsService.getSummary();

        assertThat(dto.getTotalPayments()).isEqualTo(10L);
        assertThat(dto.getCompletedPayments()).isEqualTo(8L);
        assertThat(dto.getFailedPayments()).isEqualTo(2L);
        assertThat(dto.getTotalVolume()).isEqualTo("5000.00");
        assertThat(dto.getSuccessRate()).isEqualTo("80.0%");
    }

    @Test
    void getSummary_shouldReturnZeroVolume_whenNoEvents() {
        when(repository.count()).thenReturn(0L);
        when(repository.countByStatus(any())).thenReturn(0L);

        // MongoDB returns no result document when the collection is empty
        AggregationResults<Document> emptyResult = buildMockResults(List.of());
        when(mongoTemplate.aggregate(any(Aggregation.class), eq(PaymentEventRecord.class), eq(Document.class)))
                .thenReturn(emptyResult);

        AnalyticsSummaryDTO dto = analyticsService.getSummary();

        assertThat(dto.getTotalVolume()).isEqualTo("0.00");
        assertThat(dto.getSuccessRate()).isEqualTo("0.0%");
    }

    // ── getDailyVolume ───────────────────────────────────────────────────────

    @Test
    void getDailyVolume_shouldReturnGroupedResults() {
        Document day1 = new Document("_id", "2025-01-15").append("count", 3).append("totalAmount", "1500.00");
        Document day2 = new Document("_id", "2025-01-16").append("count", 2).append("totalAmount", "800.50");
        AggregationResults<Document> aggResult = buildMockResults(List.of(day1, day2));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq(PaymentEventRecord.class), eq(Document.class)))
                .thenReturn(aggResult);

        List<DailyVolumeDTO> volume = analyticsService.getDailyVolume(
                LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 16));

        assertThat(volume).hasSize(2);
        assertThat(volume.get(0).getDate()).isEqualTo("2025-01-15");
        assertThat(volume.get(0).getCount()).isEqualTo(3L);
        assertThat(volume.get(0).getVolume()).isEqualTo("1500.00");
        assertThat(volume.get(1).getDate()).isEqualTo("2025-01-16");
        assertThat(volume.get(1).getVolume()).isEqualTo("800.50");
    }

    @Test
    void getDailyVolume_shouldThrow_whenFromIsAfterTo() {
        assertThatThrownBy(() ->
                analyticsService.getDailyVolume(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("'from' date must be before");
    }

    // ── getTopAccounts ───────────────────────────────────────────────────────

    @Test
    void getTopAccounts_shouldReturnTopSenders() {
        Document acc1 = new Document("_id", "ACC-001").append("totalSent", "15000.00");
        Document acc2 = new Document("_id", "ACC-002").append("totalSent", "9500.00");
        AggregationResults<Document> aggResult = buildMockResults(List.of(acc1, acc2));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq(PaymentEventRecord.class), eq(Document.class)))
                .thenReturn(aggResult);

        List<TopAccountDTO> top = analyticsService.getTopAccounts(5);

        assertThat(top).hasSize(2);
        assertThat(top.get(0).getAccountId()).isEqualTo("ACC-001");
        assertThat(top.get(0).getTotalVolume()).isEqualTo("15000.00");
    }

    @Test
    void getTopAccounts_shouldDefaultTo10_whenLimitIsZero() {
        AggregationResults<Document> emptyResult = buildMockResults(List.of());
        when(mongoTemplate.aggregate(any(Aggregation.class), eq(PaymentEventRecord.class), eq(Document.class)))
                .thenReturn(emptyResult);

        // limit=0 is coerced to 10 internally — just verify no exception is thrown
        List<TopAccountDTO> top = analyticsService.getTopAccounts(0);
        assertThat(top).isEmpty();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private PaymentEventDTO buildEvent(String paymentId, String status, String amount) {
        PaymentEventDTO event = new PaymentEventDTO();
        event.setPaymentId(paymentId);
        event.setFromAccountId("ACC-001");
        event.setToAccountId("ACC-002");
        event.setAmount(amount);
        event.setStatus(status);
        event.setType("TRANSFER");
        event.setOccurredAt("2025-01-15T10:30:00");
        return event;
    }

    // Builds a mock AggregationResults. Must be called BEFORE thenReturn() —
    // calling mock/when inside a thenReturn() argument causes UnfinishedStubbing.
    // lenient() suppresses UnnecessaryStubbingException — this helper stubs both getMappedResults()
    // and getUniqueMappedResult() so it works for any caller, but each individual test only triggers one.
    @SuppressWarnings("unchecked")
    private AggregationResults<Document> buildMockResults(List<Document> docs) {
        AggregationResults<Document> results = mock(AggregationResults.class);
        lenient().when(results.getMappedResults()).thenReturn(docs);
        lenient().when(results.getUniqueMappedResult()).thenReturn(docs.isEmpty() ? null : docs.get(0));
        return results;
    }
}
