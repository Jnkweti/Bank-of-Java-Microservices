package com.pm.notificationservice.Mapper;

import com.pm.notificationservice.DTO.NotificationResponseDTO;
import com.pm.notificationservice.Enum.NotificationType;
import com.pm.notificationservice.model.Notification;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationMapperTest {

    @Test
    void toDTO_shouldMapAllFieldsCorrectly() {
        Notification n = new Notification();
        n.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        n.setPaymentId("pay-001");
        n.setFromAccountId("acc-001");
        n.setToAccountId("acc-002");
        n.setType(NotificationType.PAYMENT_SUCCESS);
        n.setMessage("Your payment was successful.");
        n.setSentAt(LocalDateTime.of(2025, 6, 1, 12, 0, 0));

        NotificationResponseDTO result = NotificationMapper.toDTO(n);

        assertThat(result.getId()).isEqualTo("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        assertThat(result.getPaymentId()).isEqualTo("pay-001");
        assertThat(result.getFromAccountId()).isEqualTo("acc-001");
        assertThat(result.getToAccountId()).isEqualTo("acc-002");
        assertThat(result.getType()).isEqualTo("PAYMENT_SUCCESS");
        assertThat(result.getMessage()).isEqualTo("Your payment was successful.");
        assertThat(result.getSentAt()).isNotNull();
    }
}
