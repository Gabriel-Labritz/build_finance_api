CREATE TABLE refresh_tokens (
    id         BINARY(16)   NOT NULL,
    refresh_token      VARCHAR(255) NOT NULL,
    expires_at DATETIME(6)  NOT NULL,
    revoked    TINYINT(1)   NOT NULL DEFAULT 0,
    user_id    BINARY(16)   NOT NULL,

    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_token UNIQUE (refresh_token),
    CONSTRAINT fk_refresh_tokens_user_id
        FOREIGN KEY (user_id) REFERENCES users(id)
);