package com.pm.notificationservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Local copy of the event schema published by payment-service.
// In a microservices architecture, each consumer owns its own copy of the
// event schema — services must never import each other's JARs.
// Both copies must stay in sync by convention whenever the schema changes.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEventDTO {
    private String paymentId;
    private String fromAccountId;
    private String toAccountId;
    private String amount;
    private String status;
    private String type;
    private String occurredAt;
}
