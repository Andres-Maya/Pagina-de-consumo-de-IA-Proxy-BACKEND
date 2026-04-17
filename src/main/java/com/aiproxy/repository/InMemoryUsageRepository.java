package com.aiproxy.repository;

import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory implementation of UsageRepository using ConcurrentHashMap.
 * Thread-safe implementation for tracking request counts and token usage.
 * 
 * This implementation uses:
 * - ConcurrentHashMap for thread-safe map operations
 * - AtomicInteger for thread-safe counter operations
 */
@Repository
public class InMemoryUsageRepository implements UsageRepository {
    
    // Track request counts per user for rate limiting (resets every minute)
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts;
    
    // Track monthly token usage per user for quota management (resets monthly)
    private final ConcurrentHashMap<String, AtomicInteger> monthlyTokenUsage;
    
    public InMemoryUsageRepository() {
        this.requestCounts = new ConcurrentHashMap<>();
        this.monthlyTokenUsage = new ConcurrentHashMap<>();
    }
    
    @Override
    public void incrementRequestCount(String userId) {
        requestCounts.computeIfAbsent(userId, k -> new AtomicInteger(0))
                    .incrementAndGet();
    }
    
    @Override
    public int getRequestCount(String userId) {
        AtomicInteger count = requestCounts.get(userId);
        return count != null ? count.get() : 0;
    }
    
    @Override
    public void resetRequestCounts() {
        requestCounts.clear();
    }
    
    @Override
    public void addTokenUsage(String userId, int tokens) {
        monthlyTokenUsage.computeIfAbsent(userId, k -> new AtomicInteger(0))
                        .addAndGet(tokens);
    }
    
    @Override
    public int getMonthlyTokenUsage(String userId) {
        AtomicInteger usage = monthlyTokenUsage.get(userId);
        return usage != null ? usage.get() : 0;
    }
    
    @Override
    public void resetMonthlyUsage() {
        monthlyTokenUsage.clear();
    }
}
