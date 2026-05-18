package com.gabriellabritz.build_finance_api.infra.exceptions.auth;

public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException(String message) {
        super(message);
    }
}
