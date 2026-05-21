package com.gabriellabritz.build_finance_api.infra.exceptions.jwt;

public class JwtGenerationException extends RuntimeException {
    public JwtGenerationException(String message) {
        super(message);
    }
}
