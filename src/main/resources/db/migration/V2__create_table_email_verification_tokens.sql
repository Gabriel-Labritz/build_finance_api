CREATE TABLE email_verification_tokens (
    id         BINARY(16)   NOT NULL,
    token      VARCHAR(36)  NOT NULL,
    expires_at DATETIME(6)  NOT NULL,
    used       TINYINT(1)   NOT NULL DEFAULT 0,
    user_id    BINARY(16)   NOT NULL,

    CONSTRAINT pk_email_verification_tokens PRIMARY KEY (id),
    CONSTRAINT uq_email_verification_tokens_token UNIQUE (token),
    CONSTRAINT uq_email_verification_tokens_user_id UNIQUE (user_id),
    CONSTRAINT fk_email_verification_tokens_user_id
        FOREIGN KEY (user_id) REFERENCES users(id)
);