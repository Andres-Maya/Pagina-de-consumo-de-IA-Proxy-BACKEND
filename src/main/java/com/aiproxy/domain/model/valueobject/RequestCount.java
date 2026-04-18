package com.aiproxy.domain.model.valueobject;

import java.time.Instant;

public final class RequestCount {
    private final int value;
    private final Instant resetTimestamp;

    public RequestCount(int value, Instant resetTimestamp) {
        if (value < 0) {
            throw new IllegalArgumentException("Request count cannot be negative");
        }
        this.value = value;
        this.resetTimestamp = resetTimestamp != null ? resetTimestamp : Instant.now();
    }

    public int getValue() {
        return value;
    }

    public Instant getResetTimestamp() {
        return resetTimestamp;
    }

    public RequestCount increment() {
        return new RequestCount(this.value + 1, this.resetTimestamp);
    }

    public RequestCount reset() {
        return new RequestCount(0, Instant.now());
    }

    public boolean isWithinLimit(int limit) {
        return value < limit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestCount that = (RequestCount) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
}
