package com.pm.paymentservice.Kafka;

import com.pm.paymentservice.DTO.PaymentEventDTO;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);

    private final KafkaTemplate<String, PaymentEventDTO> kafkaTemplate;
    private final String topic;

    // @Value cannot be used with @AllArgsConstructor — we define the constructor
    // manually so Spring injects the @Value alongside the KafkaTemplate bean.
    public PaymentEventProducer(
            KafkaTemplate<String, PaymentEventDTO> kafkaTemplate,
            @Value("${kafka.topic.payment-processed}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    // Publishes a payment event to Kafka.
    // The paymentId is used as the message key — Kafka uses the key to determine
    // which partition the message lands on. Using the paymentId guarantees that
    // all events for the same payment are always in the same partition,
    // preserving ordering if a payment ever produces multiple events.
    //
    // This method is intentionally fire-and-forget:
    // A Kafka failure must NOT affect the payment outcome. The transaction has
    // already been committed to the DB. Log the error and move on.
    public void publishPaymentEvent(PaymentEventDTO event) {
        try {
            kafkaTemplate.send(topic, event.getPaymentId(), event);
            log.info("Published payment event: paymentId={} status={}", event.getPaymentId(), event.getStatus());
        } catch (Exception e) {
            log.error("Failed to publish payment event for paymentId={}: {}", event.getPaymentId(), e.getMessage());
        }
    }
}
