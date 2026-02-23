package com.pm.paymentservice.Enum;

// TRANSFER    → money moves between two existing accounts
// DEPOSIT     → money added to an account (e.g. from external source)
// WITHDRAWAL  → money removed from an account (e.g. to external destination)
public enum PaymentType {
    TRANSFER,
    DEPOSIT,
    WITHDRAWAL
}
