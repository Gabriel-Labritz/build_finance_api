package com.gabriellabritz.build_finance_api.infra.exceptions.two_factor_auth;

public class TwoFactorSecretNotFoundException extends RuntimeException {
    public TwoFactorSecretNotFoundException(String message) {
        super(message);
    }
}
