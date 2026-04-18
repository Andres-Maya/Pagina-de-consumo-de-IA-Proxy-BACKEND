package com.aiproxy.domain.exception;

public class ThrottlingExceededException extends RuntimeException {
    private final long retryAfterSeconds;

    public ThrottlingExceededException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
