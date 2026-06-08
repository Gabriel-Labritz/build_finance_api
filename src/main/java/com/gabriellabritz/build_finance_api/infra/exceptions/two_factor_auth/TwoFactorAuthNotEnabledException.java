package com.gabriellabritz.build_finance_api.infra.exceptions.two_factor_auth;

public class TwoFactorAuthNotEnabledException extends RuntimeException {
    public TwoFactorAuthNotEnabledException(String message) {
        super(message);
    }
}
