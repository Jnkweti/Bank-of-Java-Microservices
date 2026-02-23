package com.pm.paymentservice.DTO;

import lombok.Data;

@Data
public class PaymentResponseDTO {
    private String paymentId;
    private String fromAccountId;
    private String toAccountId;
    private String amount;
    private String status;
    private String type;
    private String description;
    private String createdAt;
    private String updatedAt;
}
