CREATE TABLE notifications (
    id              UUID PRIMARY KEY,
    payment_id      VARCHAR(255) NOT NULL UNIQUE,
    from_account_id VARCHAR(255) NOT NULL,
    to_account_id   VARCHAR(255) NOT NULL,
    type            VARCHAR(255) NOT NULL,
    message         VARCHAR(500) NOT NULL,
    sent_at         TIMESTAMP    NOT NULL
);
