package com.pm.notificationservice.Service;

import com.pm.notificationservice.DTO.NotificationResponseDTO;
import com.pm.notificationservice.DTO.PaymentEventDTO;
import com.pm.notificationservice.Enum.NotificationType;
import com.pm.notificationservice.Repository.notificationRepo;
import com.pm.notificationservice.model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private notificationRepo repository;
    @InjectMocks private NotificationService notificationService;

    private PaymentEventDTO successEvent;
    private PaymentEventDTO failedEvent;
    private Notification savedNotification;

    @BeforeEach
    void setUp() {
        successEvent = new PaymentEventDTO(
                "pay-001", "acc-from", "acc-to",
                "150.00", "COMPLETED", "TRANSFER", "2025-06-01T10:00:00");

        failedEvent = new PaymentEventDTO(
                "pay-002", "acc-from", "acc-to",
                "150.00", "FAILED", "TRANSFER", "2025-06-01T10:00:05");

        savedNotification = new Notification();
        savedNotification.setId(UUID.randomUUID());
        savedNotification.setPaymentId("pay-001");
        savedNotification.setFromAccountId("acc-from");
        savedNotification.setToAccountId("acc-to");
        savedNotification.setType(NotificationType.PAYMENT_SUCCESS);
        savedNotification.setMessage("Payment successful.");
        savedNotification.setSentAt(LocalDateTime.now());
    }

    // --- sendPaymentSuccessNotification ---

    @Test
    void sendPaymentSuccessNotification_shouldSaveNotification_whenNotDuplicate() {
        when(repository.existsByPaymentId("pay-001")).thenReturn(false);

        notificationService.sendPaymentSuccessNotification(successEvent);

        verify(repository).save(any(Notification.class));
    }

    @Test
    void sendPaymentSuccessNotification_shouldSaveWithCorrectType() {
        when(repository.existsByPaymentId("pay-001")).thenReturn(false);
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.sendPaymentSuccessNotification(successEvent);

        verify(repository).save(argThat(n ->
                n.getType() == NotificationType.PAYMENT_SUCCESS &&
                n.getPaymentId().equals("pay-001") &&
                n.getFromAccountId().equals("acc-from")
        ));
    }

    @Test
    void sendPaymentSuccessNotification_shouldSkip_whenDuplicate() {
        when(repository.existsByPaymentId("pay-001")).thenReturn(true);

        notificationService.sendPaymentSuccessNotification(successEvent);

        // Idempotency: no save should occur for a duplicate event
        verify(repository, never()).save(any());
    }

    // --- sendPaymentFailedNotification ---

    @Test
    void sendPaymentFailedNotification_shouldSaveNotification_whenNotDuplicate() {
        when(repository.existsByPaymentId("pay-002")).thenReturn(false);

        notificationService.sendPaymentFailedNotification(failedEvent);

        verify(repository).save(any(Notification.class));
    }

    @Test
    void sendPaymentFailedNotification_shouldSaveWithCorrectType() {
        when(repository.existsByPaymentId("pay-002")).thenReturn(false);

        notificationService.sendPaymentFailedNotification(failedEvent);

        verify(repository).save(argThat(n ->
                n.getType() == NotificationType.PAYMENT_FAILED &&
                n.getPaymentId().equals("pay-002")
        ));
    }

    @Test
    void sendPaymentFailedNotification_shouldSkip_whenDuplicate() {
        when(repository.existsByPaymentId("pay-002")).thenReturn(true);

        notificationService.sendPaymentFailedNotification(failedEvent);

        verify(repository, never()).save(any());
    }

    // --- getNotificationsByAccount ---

    @Test
    void getNotificationsByAccount_shouldReturnMappedList() {
        when(repository.findByFromAccountIdOrToAccountId("acc-from", "acc-from"))
                .thenReturn(List.of(savedNotification));

        List<NotificationResponseDTO> result = notificationService.getNotificationsByAccount("acc-from");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPaymentId()).isEqualTo("pay-001");
        assertThat(result.get(0).getType()).isEqualTo("PAYMENT_SUCCESS");
    }

    @Test
    void getNotificationsByAccount_shouldReturnEmptyList_whenNoneFound() {
        when(repository.findByFromAccountIdOrToAccountId(anyString(), anyString()))
                .thenReturn(List.of());

        assertThat(notificationService.getNotificationsByAccount("acc-none")).isEmpty();
    }
}
