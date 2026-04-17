package com.aiproxy.repository;

import com.aiproxy.model.SubscriptionPlan;

/**
 * Repository interface for managing subscription plan data.
 * Handles storage and retrieval of user subscription plans.
 */
public interface PlanRepository {
    
    /**
     * Retrieves the subscription plan for a user.
     * If no plan is stored, returns the default FREE plan.
     *
     * @param userId the unique identifier of the user
     * @return the user's subscription plan (defaults to FREE)
     */
    SubscriptionPlan getPlan(String userId);
    
    /**
     * Updates the subscription plan for a user.
     *
     * @param userId the unique identifier of the user
     * @param plan the new subscription plan to assign
     */
    void updatePlan(String userId, SubscriptionPlan plan);
}
