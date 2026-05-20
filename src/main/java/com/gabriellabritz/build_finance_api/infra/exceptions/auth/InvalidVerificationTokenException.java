package com.gabriellabritz.build_finance_api.infra.exceptions.auth;

public class InvalidVerificationTokenException extends RuntimeException {
    public InvalidVerificationTokenException(String message) {
        super(message);
    }
}
