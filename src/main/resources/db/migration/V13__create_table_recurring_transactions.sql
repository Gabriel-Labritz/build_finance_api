CREATE TABLE recurring_transactions (
    id                  BINARY(16)      NOT NULL,
    user_id             BINARY(16)      NOT NULL,
    category_id         BINARY(16)      NOT NULL,
    title               VARCHAR(255)    NOT NULL,
    description         VARCHAR(255)    NULL,
    amount              DECIMAL(19, 2)  NOT NULL,
    type                VARCHAR(10)     NOT NULL,
    frequency           VARCHAR(10)     NOT NULL,
    start_date          DATE            NOT NULL,
    end_date            DATE            NULL,
    next_execution_date DATE            NOT NULL,
    last_execution_date DATE            NULL,
    active              TINYINT(1)      NOT NULL,

    CONSTRAINT pk_recurring_transactions PRIMARY KEY (id),
    CONSTRAINT fk_recurring_transactions_user     FOREIGN KEY (user_id)     REFERENCES users(id),
    CONSTRAINT fk_recurring_transactions_category FOREIGN KEY (category_id) REFERENCES categories(id)
);