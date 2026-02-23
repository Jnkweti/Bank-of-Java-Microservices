package com.pm.analyticsservice.Kafka;

import com.pm.analyticsservice.DTO.PaymentEventDTO;
import com.pm.analyticsservice.Service.AnalyticsService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final AnalyticsService analyticsService;

    // analytics-group is independent of notification-group.
    // Both groups receive every message — Kafka fans out to all consumer groups.
    @KafkaListener(
            topics = "${kafka.topic.payment-processed}",
            groupId = "analytics-group"
    )
    public void consume(PaymentEventDTO event) {
        log.info("Analytics received event: paymentId={} status={}", event.getPaymentId(), event.getStatus());
        analyticsService.recordEvent(event);
    }
}
