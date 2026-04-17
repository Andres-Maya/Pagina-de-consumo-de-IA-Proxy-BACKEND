package com.aiproxy.repository;

/**
 * Repository interface for managing usage tracking data.
 * Handles both rate limiting (request counts) and quota management (token usage).
 */
public interface UsageRepository {
    
    /**
     * Increments the request count for a user in the current minute.
     * Used for rate limiting enforcement.
     *
     * @param userId the unique identifier of the user
     */
    void incrementRequestCount(String userId);
    
    /**
     * Retrieves the current request count for a user in the current minute.
     * Used to check rate limit compliance.
     *
     * @param userId the unique identifier of the user
     * @return the number of requests made in the current minute
     */
    int getRequestCount(String userId);
    
    /**
     * Resets all users' request counts to zero.
     * Called by the scheduled task every minute.
     */
    void resetRequestCounts();
    
    /**
     * Adds token usage to a user's monthly consumption.
     * Used for quota tracking and enforcement.
     *
     * @param userId the unique identifier of the user
     * @param tokens the number of tokens consumed
     */
    void addTokenUsage(String userId, int tokens);
    
    /**
     * Retrieves the total token usage for a user in the current month.
     * Used to check quota compliance.
     *
     * @param userId the unique identifier of the user
     * @return the total tokens consumed in the current month
     */
    int getMonthlyTokenUsage(String userId);
    
    /**
     * Resets all users' monthly token usage to zero.
     * Called by the scheduled task on the first day of each month.
     */
    void resetMonthlyUsage();
}
