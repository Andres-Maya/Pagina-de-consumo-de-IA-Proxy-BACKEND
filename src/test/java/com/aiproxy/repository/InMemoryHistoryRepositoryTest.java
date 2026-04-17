package com.aiproxy.repository;

import com.aiproxy.model.DailyUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InMemoryHistoryRepository.
 * Tests daily usage recording and retrieval, including thread-safety.
 */
class InMemoryHistoryRepositoryTest {
    
    private InMemoryHistoryRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new InMemoryHistoryRepository();
    }
    
    @Test
    void recordDailyUsage_shouldStoreUsageForDate() {
        String userId = "user1";
        LocalDate date = LocalDate.of(2024, 1, 15);
        int tokens = 1000;
        
        repository.recordDailyUsage(userId, date, tokens);
        
        List<DailyUsage> usage = repository.getDailyUsage(userId, date, date);
        assertEquals(1, usage.size());
        assertEquals(date, usage.get(0).getDate());
        assertEquals(tokens, usage.get(0).getTokensUsed());
    }
    
    @Test
    void recordDailyUsage_shouldUpdateExistingDate() {
        String userId = "user1";
        LocalDate date = LocalDate.of(2024, 1, 15);
        
        repository.recordDailyUsage(userId, date, 1000);
        repository.recordDailyUsage(userId, date, 2000);
        
        List<DailyUsage> usage = repository.getDailyUsage(userId, date, date);
        assertEquals(1, usage.size());
        assertEquals(2000, usage.get(0).getTokensUsed());
    }
    
    @Test
    void getDailyUsage_shouldReturnSevenDaysOfHistory() {
        String userId = "user1";
        LocalDate endDate = LocalDate.of(2024, 1, 20);
        LocalDate startDate = endDate.minusDays(6);
        
        // Record usage for some days
        repository.recordDailyUsage(userId, LocalDate.of(2024, 1, 15), 500);
        repository.recordDailyUsage(userId, LocalDate.of(2024, 1, 18), 1000);
        repository.recordDailyUsage(userId, LocalDate.of(2024, 1, 20), 750);
        
        List<DailyUsage> usage = repository.getDailyUsage(userId, startDate, endDate);
        
        assertEquals(7, usage.size());
    }
    
    @Test
    void getDailyUsage_shouldIncludeZeroUsageDays() {
        String userId = "user1";
        LocalDate startDate = LocalDate.of(2024, 1, 14);
        LocalDate endDate = LocalDate.of(2024, 1, 20);
        
        // Record usage for only 2 days
        repository.recordDailyUsage(userId, LocalDate.of(2024, 1, 15), 500);
        repository.recordDailyUsage(userId, LocalDate.of(2024, 1, 18), 1000);
        
        List<DailyUsage> usage = repository.getDailyUsage(userId, startDate, endDate);
        
        assertEquals(7, usage.size());
        
        // Check that days without usage have zero tokens
        assertEquals(0, usage.get(0).getTokensUsed()); // Jan 14
        assertEquals(500, usage.get(1).getTokensUsed()); // Jan 15
        assertEquals(0, usage.get(2).getTokensUsed()); // Jan 16
        assertEquals(0, usage.get(3).getTokensUsed()); // Jan 17
        assertEquals(1000, usage.get(4).getTokensUsed()); // Jan 18
        assertEquals(0, usage.get(5).getTokensUsed()); // Jan 19
        assertEquals(0, usage.get(6).getTokensUsed()); // Jan 20
    }
    
    @Test
    void getDailyUsage_shouldReturnEmptyListForNewUser() {
        String userId = "newUser";
        LocalDate startDate = LocalDate.of(2024, 1, 14);
        LocalDate endDate = LocalDate.of(2024, 1, 20);
        
        List<DailyUsage> usage = repository.getDailyUsage(userId, startDate, endDate);
        
        assertEquals(7, usage.size());
        // All days should have zero usage
        for (DailyUsage daily : usage) {
            assertEquals(0, daily.getTokensUsed());
        }
    }
    
    @Test
    void getDailyUsage_shouldReturnDatesInOrder() {
        String userId = "user1";
        LocalDate startDate = LocalDate.of(2024, 1, 14);
        LocalDate endDate = LocalDate.of(2024, 1, 20);
        
        repository.recordDailyUsage(userId, LocalDate.of(2024, 1, 15), 500);
        repository.recordDailyUsage(userId, LocalDate.of(2024, 1, 18), 1000);
        
        List<DailyUsage> usage = repository.getDailyUsage(userId, startDate, endDate);
        
        // Verify dates are in ascending order
        LocalDate expectedDate = startDate;
        for (DailyUsage daily : usage) {
            assertEquals(expectedDate, daily.getDate());
            expectedDate = expectedDate.plusDays(1);
        }
    }
    
    @Test
    void multipleUsers_shouldTrackIndependently() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        
        repository.recordDailyUsage("user1", date, 1000);
        repository.recordDailyUsage("user2", date, 2000);
        
        List<DailyUsage> user1Usage = repository.getDailyUsage("user1", date, date);
        List<DailyUsage> user2Usage = repository.getDailyUsage("user2", date, date);
        
        assertEquals(1000, user1Usage.get(0).getTokensUsed());
        assertEquals(2000, user2Usage.get(0).getTokensUsed());
    }
    
    @Test
    void getDailyUsage_shouldHandleSingleDay() {
        String userId = "user1";
        LocalDate date = LocalDate.of(2024, 1, 15);
        
        repository.recordDailyUsage(userId, date, 500);
        
        List<DailyUsage> usage = repository.getDailyUsage(userId, date, date);
        
        assertEquals(1, usage.size());
        assertEquals(date, usage.get(0).getDate());
        assertEquals(500, usage.get(0).getTokensUsed());
    }
    
    @Test
    void concurrentRecording_shouldBeThreadSafe() throws InterruptedException {
        String userId = "user1";
        LocalDate date = LocalDate.of(2024, 1, 15);
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int tokens = (i + 1) * 100;
            executor.submit(() -> {
                try {
                    repository.recordDailyUsage(userId, date, tokens);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        
        List<DailyUsage> usage = repository.getDailyUsage(userId, date, date);
        assertEquals(1, usage.size());
        // Should have one of the recorded values (last write wins)
        assertTrue(usage.get(0).getTokensUsed() >= 100 && usage.get(0).getTokensUsed() <= 1000);
    }
    
    @Test
    void concurrentReading_shouldBeThreadSafe() throws InterruptedException {
        String userId = "user1";
        LocalDate startDate = LocalDate.of(2024, 1, 14);
        LocalDate endDate = LocalDate.of(2024, 1, 20);
        
        // Pre-populate some data
        repository.recordDailyUsage(userId, LocalDate.of(2024, 1, 15), 500);
        repository.recordDailyUsage(userId, LocalDate.of(2024, 1, 18), 1000);
        
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    List<DailyUsage> usage = repository.getDailyUsage(userId, startDate, endDate);
                    assertEquals(7, usage.size());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
    }
}
