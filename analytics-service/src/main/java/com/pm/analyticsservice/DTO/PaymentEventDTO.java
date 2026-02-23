package com.pm.analyticsservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Local copy of the event schema published by payment-service.
// Each consumer service owns its copy — never import another service's JAR.
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
