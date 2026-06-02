CREATE TABLE two_factor_auth (
    id         BINARY(16)   NOT NULL,
    secret     VARCHAR(255) NOT NULL,
    user_id    BINARY(16)   NOT NULL,

    CONSTRAINT pk_two_factor_auth PRIMARY KEY (id),
    CONSTRAINT uq_two_factor_auth_user UNIQUE (user_id),
    CONSTRAINT fk_two_factor_auth_user_id
        FOREIGN KEY (user_id) REFERENCES users(id)
);