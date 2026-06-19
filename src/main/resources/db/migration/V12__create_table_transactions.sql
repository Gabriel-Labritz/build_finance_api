CREATE TABLE transactions (
    id           BINARY(16)     NOT NULL,
    type         VARCHAR(7)     NOT NULL,
    amount       DECIMAL(19, 2) NOT NULL,
    category_id  BINARY(16)     NOT NULL,
    user_id      BINARY(16)     NOT NULL,
    date         DATE           NOT NULL,
    description  VARCHAR(255),

    CONSTRAINT pk_transactions PRIMARY KEY (id),
    CONSTRAINT fk_transactions_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_transactions_user     FOREIGN KEY (user_id)     REFERENCES users(id)
);