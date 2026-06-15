CREATE TABLE categories (
    id               BINARY(16)   NOT NULL,
    name             VARCHAR(50)  NOT NULL,
    default_category TINYINT(1)   NOT NULL DEFAULT 0,
    type             VARCHAR(10)  NOT NULL,
    user_id          BINARY(16)   NULL,

    CONSTRAINT pk_categories          PRIMARY KEY (id),
    CONSTRAINT fk_categories_user_id  FOREIGN KEY (user_id) REFERENCES users(id)
);