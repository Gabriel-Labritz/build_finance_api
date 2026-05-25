package com.gabriellabritz.build_finance_api.infra.exceptions.jwt;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
