package com.aiproxy.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InMemoryUsageRepository.
 * Tests both single-threaded and concurrent operations.
 */
class InMemoryUsageRepositoryTest {
    
    private InMemoryUsageRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new InMemoryUsageRepository();
    }
    
    @Test
    void incrementRequestCount_shouldIncrementFromZero() {
        String userId = "user1";
        
        repository.incrementRequestCount(userId);
        
        assertEquals(1, repository.getRequestCount(userId));
    }
    
    @Test
    void incrementRequestCount_shouldIncrementMultipleTimes() {
        String userId = "user1";
        
        repository.incrementRequestCount(userId);
        repository.incrementRequestCount(userId);
        repository.incrementRequestCount(userId);
        
        assertEquals(3, repository.getRequestCount(userId));
    }
    
    @Test
    void getRequestCount_shouldReturnZeroForNewUser() {
        String userId = "newUser";
        
        assertEquals(0, repository.getRequestCount(userId));
    }
    
    @Test
    void resetRequestCounts_shouldClearAllCounts() {
        repository.incrementRequestCount("user1");
        repository.incrementRequestCount("user2");
        repository.incrementRequestCount("user1");
        
        repository.resetRequestCounts();
        
        assertEquals(0, repository.getRequestCount("user1"));
        assertEquals(0, repository.getRequestCount("user2"));
    }
    
    @Test
    void addTokenUsage_shouldAddTokens() {
        String userId = "user1";
        
        repository.addTokenUsage(userId, 100);
        
        assertEquals(100, repository.getMonthlyTokenUsage(userId));
    }
    
    @Test
    void addTokenUsage_shouldAccumulateTokens() {
        String userId = "user1";
        
        repository.addTokenUsage(userId, 100);
        repository.addTokenUsage(userId, 250);
        repository.addTokenUsage(userId, 50);
        
        assertEquals(400, repository.getMonthlyTokenUsage(userId));
    }
    
    @Test
    void getMonthlyTokenUsage_shouldReturnZeroForNewUser() {
        String userId = "newUser";
        
        assertEquals(0, repository.getMonthlyTokenUsage(userId));
    }
    
    @Test
    void resetMonthlyUsage_shouldClearAllUsage() {
        repository.addTokenUsage("user1", 1000);
        repository.addTokenUsage("user2", 2000);
        repository.addTokenUsage("user1", 500);
        
        repository.resetMonthlyUsage();
        
        assertEquals(0, repository.getMonthlyTokenUsage("user1"));
        assertEquals(0, repository.getMonthlyTokenUsage("user2"));
    }
    
    @Test
    void multipleUsers_shouldTrackIndependently() {
        repository.incrementRequestCount("user1");
        repository.incrementRequestCount("user1");
        repository.incrementRequestCount("user2");
        
        repository.addTokenUsage("user1", 100);
        repository.addTokenUsage("user2", 200);
        
        assertEquals(2, repository.getRequestCount("user1"));
        assertEquals(1, repository.getRequestCount("user2"));
        assertEquals(100, repository.getMonthlyTokenUsage("user1"));
        assertEquals(200, repository.getMonthlyTokenUsage("user2"));
    }
    
    @Test
    void concurrentIncrements_shouldBeThreadSafe() throws InterruptedException {
        String userId = "user1";
        int threadCount = 10;
        int incrementsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        repository.incrementRequestCount(userId);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        
        assertEquals(threadCount * incrementsPerThread, repository.getRequestCount(userId));
    }
    
    @Test
    void concurrentTokenAdditions_shouldBeThreadSafe() throws InterruptedException {
        String userId = "user1";
        int threadCount = 10;
        int tokensPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    repository.addTokenUsage(userId, tokensPerThread);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        
        assertEquals(threadCount * tokensPerThread, repository.getMonthlyTokenUsage(userId));
    }
}
