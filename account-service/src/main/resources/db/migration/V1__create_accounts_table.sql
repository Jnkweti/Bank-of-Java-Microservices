CREATE TABLE accounts (
    id             UUID PRIMARY KEY,
    balance        NUMERIC(19,2) NOT NULL,
    customer_id    UUID          NOT NULL,
    account_number VARCHAR(255)  NOT NULL UNIQUE,
    account_name   VARCHAR(255)  NOT NULL,
    account_type   VARCHAR(20)   NOT NULL,
    status         VARCHAR(15)   NOT NULL,
    interest_rate  NUMERIC(5,2),
    opened_date    TIMESTAMP     NOT NULL,
    last_updated   TIMESTAMP
);
