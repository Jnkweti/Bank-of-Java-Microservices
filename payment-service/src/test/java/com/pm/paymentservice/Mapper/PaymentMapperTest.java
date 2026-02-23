package com.pm.paymentservice.Mapper;

import com.pm.paymentservice.DTO.PaymentRequestDTO;
import com.pm.paymentservice.DTO.PaymentResponseDTO;
import com.pm.paymentservice.Enum.PaymentStatus;
import com.pm.paymentservice.Enum.PaymentType;
import com.pm.paymentservice.model.payment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMapperTest {

    // --- toEntity ---

    @Test
    void toEntity_shouldMapAllFieldsCorrectly() {
        PaymentRequestDTO dto = new PaymentRequestDTO();
        dto.setFromAccountId("acc-001");
        dto.setToAccountId("acc-002");
        dto.setAmount("250.00");
        dto.setType(PaymentType.TRANSFER);
        dto.setDescription("Rent payment");

        payment result = PaymentMapper.toEntity(dto);

        assertThat(result.getFromAccountId()).isEqualTo("acc-001");
        assertThat(result.getToAccountId()).isEqualTo("acc-002");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(result.getType()).isEqualTo(PaymentType.TRANSFER);
        assertThat(result.getDescription()).isEqualTo("Rent payment");
        // Status must always start as PENDING — service layer enforces this invariant
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void toEntity_shouldSetPendingStatus_evenWhenDescriptionIsNull() {
        PaymentRequestDTO dto = new PaymentRequestDTO();
        dto.setFromAccountId("acc-001");
        dto.setToAccountId("acc-002");
        dto.setAmount("100.00");
        dto.setType(PaymentType.DEPOSIT);
        dto.setDescription(null);

        payment result = PaymentMapper.toEntity(dto);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getDescription()).isNull();
    }

    // --- toDTO ---

    @Test
    void toDTO_shouldMapAllFieldsCorrectly() {
        payment p = new payment();
        p.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        p.setFromAccountId("acc-001");
        p.setToAccountId("acc-002");
        p.setAmount(new BigDecimal("250.0000"));
        p.setStatus(PaymentStatus.COMPLETED);
        p.setType(PaymentType.TRANSFER);
        p.setDescription("Rent payment");
        p.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0, 0));
        p.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 10, 0, 5));

        PaymentResponseDTO result = PaymentMapper.toDTO(p);

        assertThat(result.getPaymentId()).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(result.getFromAccountId()).isEqualTo("acc-001");
        assertThat(result.getToAccountId()).isEqualTo("acc-002");
        assertThat(result.getAmount()).isEqualTo("250.0000");
        // Status and type stored as plain enum names - consumers and proto layer rely on this
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getType()).isEqualTo("TRANSFER");
        assertThat(result.getDescription()).isEqualTo("Rent payment");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }
}
