package com.pm.paymentservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Kafka event payload published to the payment-processed topic.
// All fields are String so Jackson can serialize/deserialize without
// any custom type configuration on the consumer side.
// @NoArgsConstructor is required by Jackson for deserialization.
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
