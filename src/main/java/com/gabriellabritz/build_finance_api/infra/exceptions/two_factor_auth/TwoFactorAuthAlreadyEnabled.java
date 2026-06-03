package com.gabriellabritz.build_finance_api.infra.exceptions.two_factor_auth;

public class TwoFactorAuthAlreadyEnabled extends RuntimeException {
    public TwoFactorAuthAlreadyEnabled(String message) {
        super(message);
    }
}
