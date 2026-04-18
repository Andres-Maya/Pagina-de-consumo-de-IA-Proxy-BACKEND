package com.aiproxy.domain.exception;

public class InsufficientTokensException extends RuntimeException {
    public InsufficientTokensException(String message) {
        super(message);
    }
}
