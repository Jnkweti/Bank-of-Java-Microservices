package com.pm.notificationservice.Kafka;

import com.pm.notificationservice.DTO.PaymentEventDTO;
import com.pm.notificationservice.Service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock  private NotificationService notificationService;
    @InjectMocks private PaymentEventConsumer consumer;

    @Test
    void consume_shouldCallSuccessHandler_whenStatusIsCompleted() {
        PaymentEventDTO event = new PaymentEventDTO(
                "pay-001", "acc-from", "acc-to",
                "100.00", "COMPLETED", "TRANSFER", "2025-06-01T10:00:00");

        consumer.consume(event);

        verify(notificationService).sendPaymentSuccessNotification(event);
        verify(notificationService, never()).sendPaymentFailedNotification(any());
    }

    @Test
    void consume_shouldCallFailedHandler_whenStatusIsFailed() {
        PaymentEventDTO event = new PaymentEventDTO(
                "pay-002", "acc-from", "acc-to",
                "100.00", "FAILED", "TRANSFER", "2025-06-01T10:00:05");

        consumer.consume(event);

        verify(notificationService).sendPaymentFailedNotification(event);
        verify(notificationService, never()).sendPaymentSuccessNotification(any());
    }

    @Test
    void consume_shouldNotCallAnyHandler_whenStatusIsUnrecognised() {
        PaymentEventDTO event = new PaymentEventDTO(
                "pay-003", "acc-from", "acc-to",
                "100.00", "PENDING", "TRANSFER", "2025-06-01T10:00:00");

        consumer.consume(event);

        verify(notificationService, never()).sendPaymentSuccessNotification(any());
        verify(notificationService, never()).sendPaymentFailedNotification(any());
    }
}
