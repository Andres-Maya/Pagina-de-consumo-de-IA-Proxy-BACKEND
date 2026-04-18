package com.aiproxy.domain.model.valueobject;

public final class TokenCount {
    private final int value;

    public TokenCount(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Token count cannot be negative");
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public TokenCount subtract(TokenCount other) {
        return new TokenCount(Math.max(0, this.value - other.value));
    }

    public boolean isExhausted() {
        return value <= 0;
    }

    public boolean hasAtLeast(int amount) {
        return value >= amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenCount that = (TokenCount) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
}
