CREATE TABLE customers (
    id            UUID PRIMARY KEY,
    first_name    VARCHAR(255) NOT NULL,
    last_name     VARCHAR(255) NOT NULL,
    address       VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    birth_date    DATE         NOT NULL,
    register_date DATE         NOT NULL
);
