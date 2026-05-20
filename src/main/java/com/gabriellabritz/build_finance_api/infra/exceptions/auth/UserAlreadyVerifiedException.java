package com.gabriellabritz.build_finance_api.infra.exceptions.auth;

public class UserAlreadyVerifiedException extends RuntimeException {
    public UserAlreadyVerifiedException(String message) {
        super(message);
    }
}
