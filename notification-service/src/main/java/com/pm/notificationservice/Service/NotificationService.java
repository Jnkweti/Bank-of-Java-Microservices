package com.pm.notificationservice.Service;

import com.pm.notificationservice.DTO.NotificationResponseDTO;
import com.pm.notificationservice.DTO.PaymentEventDTO;
import com.pm.notificationservice.Enum.NotificationType;
import com.pm.notificationservice.Mapper.NotificationMapper;
import com.pm.notificationservice.Repository.notificationRepo;
import com.pm.notificationservice.model.Notification;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final notificationRepo repository;

    // ── Kafka-triggered methods ───────────────────────────────────────────────

    public void sendPaymentSuccessNotification(PaymentEventDTO event) {
        if (isDuplicate(event.getPaymentId())) return;

        Notification n = new Notification();
        n.setPaymentId(event.getPaymentId());
        n.setFromAccountId(event.getFromAccountId());
        n.setToAccountId(event.getToAccountId());
        n.setType(NotificationType.PAYMENT_SUCCESS);
        n.setMessage(String.format(
                "Your payment of %s has been successfully sent from account %s to account %s.",
                event.getAmount(), event.getFromAccountId(), event.getToAccountId()));

        repository.save(n);
        // In production this would dispatch an email/SMS via a provider (SES, Twilio, etc).
        // For portfolio purposes we log the notification to confirm the event was received.
        log.info("[NOTIFY] SUCCESS — paymentId={} amount={} from={} to={}",
                event.getPaymentId(), event.getAmount(),
                event.getFromAccountId(), event.getToAccountId());
    }

    public void sendPaymentFailedNotification(PaymentEventDTO event) {
        if (isDuplicate(event.getPaymentId())) return;

        Notification n = new Notification();
        n.setPaymentId(event.getPaymentId());
        n.setFromAccountId(event.getFromAccountId());
        n.setToAccountId(event.getToAccountId());
        n.setType(NotificationType.PAYMENT_FAILED);
        n.setMessage(String.format(
                "Your payment of %s from account %s could not be completed. Please check your balance and try again.",
                event.getAmount(), event.getFromAccountId()));

        repository.save(n);
        log.warn("[NOTIFY] FAILED — paymentId={} amount={} from={}",
                event.getPaymentId(), event.getAmount(), event.getFromAccountId());
    }

    // ── REST-accessible methods ───────────────────────────────────────────────

    // Returns all notifications involving a given account (as sender or receiver).
    // The same accountId is passed for both parameters because the repo query
    // uses OR across two columns — same pattern as payment-service's getPaymentsByAccount.
    public List<NotificationResponseDTO> getNotificationsByAccount(String accountId) {
        return repository.findByFromAccountIdOrToAccountId(accountId, accountId)
                .stream()
                .map(NotificationMapper::toDTO)
                .toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    // Guards against duplicate processing caused by Kafka at-least-once delivery.
    // If a notification for this paymentId already exists, we skip silently.
    private boolean isDuplicate(String paymentId) {
        if (repository.existsByPaymentId(paymentId)) {
            log.warn("Duplicate event received for paymentId={} — skipping", paymentId);
            return true;
        }
        return false;
    }
}
