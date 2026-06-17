package com.gabriellabritz.build_finance_api.infra.exceptions.categories;

public class DefaultCategoryException extends RuntimeException {
    public DefaultCategoryException(String message) {
        super(message);
    }
}
