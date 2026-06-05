package com.gabriellabritz.build_finance_api.infra.exceptions.two_factor_auth;

public class TwoFactorAuthNotEnabled extends RuntimeException {
    public TwoFactorAuthNotEnabled(String message) {
        super(message);
    }
}
