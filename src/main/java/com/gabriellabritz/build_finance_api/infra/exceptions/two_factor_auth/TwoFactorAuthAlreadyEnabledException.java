package com.gabriellabritz.build_finance_api.infra.exceptions.two_factor_auth;

public class TwoFactorAuthAlreadyEnabledException extends RuntimeException {
    public TwoFactorAuthAlreadyEnabledException(String message) {
        super(message);
    }
}
