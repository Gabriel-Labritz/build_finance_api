package com.gabriellabritz.build_finance_api.infra.exceptions.jwt;

public class InvalidPreAuthTokenException extends RuntimeException {
    public InvalidPreAuthTokenException(String message) {
        super(message);
    }
}
