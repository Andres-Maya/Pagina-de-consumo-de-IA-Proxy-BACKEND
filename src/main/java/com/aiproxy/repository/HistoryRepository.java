package com.aiproxy.repository;

import com.aiproxy.model.DailyUsage;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for managing historical usage data.
 * Handles daily token consumption tracking for usage history retrieval.
 */
public interface HistoryRepository {
    
    /**
     * Records daily token usage for a specific user and date.
     * If usage already exists for the date, it will be updated.
     *
     * @param userId the unique identifier of the user
     * @param date the date of the usage
     * @param tokens the number of tokens consumed on that date
     */
    void recordDailyUsage(String userId, LocalDate date, int tokens);
    
    /**
     * Retrieves daily usage history for a user within a date range.
     * Returns usage data for all dates in the range, including days with zero usage.
     *
     * @param userId the unique identifier of the user
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @return list of daily usage records ordered by date
     */
    List<DailyUsage> getDailyUsage(String userId, LocalDate startDate, LocalDate endDate);
}
