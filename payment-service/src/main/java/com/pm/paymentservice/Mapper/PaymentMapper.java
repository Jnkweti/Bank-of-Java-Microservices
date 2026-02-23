package com.pm.paymentservice.Mapper;

import com.pm.paymentservice.DTO.PaymentRequestDTO;
import com.pm.paymentservice.DTO.PaymentResponseDTO;
import com.pm.paymentservice.Enum.PaymentStatus;
import com.pm.paymentservice.model.payment;

import java.math.BigDecimal;

public class PaymentMapper {

    public static payment toEntity(PaymentRequestDTO dto) {
        payment p = new payment();
        p.setFromAccountId(dto.getFromAccountId());
        p.setToAccountId(dto.getToAccountId());
        p.setAmount(new BigDecimal(dto.getAmount()));
        p.setType(dto.getType());
        p.setDescription(dto.getDescription());
        p.setStatus(PaymentStatus.PENDING);
        return p;
    }

    public static PaymentResponseDTO toDTO(payment p) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setPaymentId(p.getId().toString());
        dto.setFromAccountId(p.getFromAccountId());
        dto.setToAccountId(p.getToAccountId());
        dto.setAmount(p.getAmount().toString());
        dto.setStatus(p.getStatus().name());
        dto.setType(p.getType().name());
        dto.setDescription(p.getDescription());
        dto.setCreatedAt(p.getCreatedAt().toString());
        dto.setUpdatedAt(p.getUpdatedAt().toString());
        return dto;
    }
}
