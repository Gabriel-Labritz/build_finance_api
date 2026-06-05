package com.gabriellabritz.build_finance_api.infra.exceptions.two_factor_auth;

public class InvalidA2FCodeException extends RuntimeException {
    public InvalidA2FCodeException(String message) {
        super(message);
    }
}
