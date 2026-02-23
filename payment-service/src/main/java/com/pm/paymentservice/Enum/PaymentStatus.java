package com.pm.paymentservice.Enum;

// Tracks the lifecycle of a payment.
// PENDING  → payment has been initiated but accounts not yet updated
// COMPLETED → both debit and credit succeeded
// FAILED   → something went wrong - source account was not charged (or charge was reversed)
public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED
}
