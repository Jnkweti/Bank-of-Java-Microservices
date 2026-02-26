CREATE TABLE payments (
    id              UUID PRIMARY KEY,
    from_account_id VARCHAR(255)  NOT NULL,
    to_account_id   VARCHAR(255)  NOT NULL,
    amount          NUMERIC(19,4) NOT NULL,
    status          VARCHAR(255)  NOT NULL,
    type            VARCHAR(255)  NOT NULL,
    description     VARCHAR(255),
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP
);
