package com.pm.notificationservice.Kafka;

import com.pm.notificationservice.DTO.PaymentEventDTO;
import com.pm.notificationservice.Service.NotificationService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final NotificationService notificationService;

    // @KafkaListener binds this method to the payment-processed topic.
    // Spring Kafka deserializes each incoming message into a PaymentEventDTO
    // using the JsonDeserializer configured in application.yml.
    //
    // groupId = "notification-group" means this service maintains its own
    // consumer offset independently of analytics-service (which uses a
    // different group). Both groups receive every message on the topic.
    @KafkaListener(
            topics = "${kafka.topic.payment-processed}",
            groupId = "notification-group"
    )
    public void consume(PaymentEventDTO event) {
        log.info("Received payment event: paymentId={} status={}", event.getPaymentId(), event.getStatus());

        // Route to the correct notification handler based on payment outcome.
        // Status values match the enum names published by payment-service: PAY_COMPLETED, PAY_FAILED.
        switch (event.getStatus()) {
            case "COMPLETED" -> notificationService.sendPaymentSuccessNotification(event);
            case "FAILED"    -> notificationService.sendPaymentFailedNotification(event);
            default          -> log.warn("Unrecognised payment status: {} for paymentId={}",
                                         event.getStatus(), event.getPaymentId());
        }
    }
}
