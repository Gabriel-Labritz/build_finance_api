package com.gabriellabritz.build_finance_api.infra.exceptions.email;

public class EmailSendException extends RuntimeException {
    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
