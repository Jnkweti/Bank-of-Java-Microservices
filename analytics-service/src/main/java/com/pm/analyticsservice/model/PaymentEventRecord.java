package com.pm.analyticsservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// MongoDB document — stored in the "payment_events" collection.
// No schema migrations needed. Adding a new field to this class
// simply starts populating it on new documents; old documents
// just won't have the field (MongoDB is schemaless).
@Document(collection = "payment_events")
@Data
public class PaymentEventRecord {

    @Id
    private String id;

    // @Indexed(unique = true) creates a unique index in MongoDB on this field.
    // Serves the same deduplication purpose as the UNIQUE constraint in SQL —
    // a second insert with the same paymentId will throw a DuplicateKeyException.
    @Indexed(unique = true)
    private String paymentId;

    private String fromAccountId;
    private String toAccountId;

    // BigDecimal stored as a Decimal128 BSON type in MongoDB.
    // Preserves precision for financial arithmetic — same rule as SQL.
    private BigDecimal amount;

    private String status;
    private String type;
    private LocalDateTime occurredAt;
    private LocalDateTime recordedAt;
}
