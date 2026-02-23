package com.pm.paymentservice.DTO;

import com.pm.paymentservice.Enum.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequestDTO {

    @NotBlank(message = "Source account id is required")
    private String fromAccountId;

    @NotBlank(message = "Destination account id is required")
    private String toAccountId;

    // String to match account-service's balance representation.
    // Parsed into BigDecimal inside the service layer.
    @NotBlank(message = "Amount is required")
    private String amount;

    @NotNull(message = "Payment type is required")
    private PaymentType type;

    // Optional - caller can describe the purpose of the payment
    private String description;
}
