package com.aiproxy.repository;

import com.aiproxy.model.DailyUsage;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of HistoryRepository using ConcurrentHashMap.
 * Thread-safe implementation for tracking daily token usage history.
 * 
 * This implementation uses:
 * - ConcurrentHashMap for thread-safe map operations
 * - Nested map structure: userId -> (date -> tokenCount)
 */
@Repository
public class InMemoryHistoryRepository implements HistoryRepository {
    
    // Track daily usage per user: userId -> (date -> tokens)
    private final ConcurrentHashMap<String, ConcurrentHashMap<LocalDate, Integer>> dailyUsageByUser;
    
    public InMemoryHistoryRepository() {
        this.dailyUsageByUser = new ConcurrentHashMap<>();
    }
    
    @Override
    public void recordDailyUsage(String userId, LocalDate date, int tokens) {
        dailyUsageByUser.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                       .put(date, tokens);
    }
    
    @Override
    public List<DailyUsage> getDailyUsage(String userId, LocalDate startDate, LocalDate endDate) {
        List<DailyUsage> result = new ArrayList<>();
        Map<LocalDate, Integer> userUsage = dailyUsageByUser.get(userId);
        
        // Iterate through all dates in the range
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            int tokensUsed = 0;
            
            // Get tokens for this date if they exist
            if (userUsage != null && userUsage.containsKey(currentDate)) {
                tokensUsed = userUsage.get(currentDate);
            }
            
            result.add(new DailyUsage(currentDate, tokensUsed));
            currentDate = currentDate.plusDays(1);
        }
        
        return result;
    }
}
