package com.aiproxy.repository;

import com.aiproxy.model.SubscriptionPlan;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of PlanRepository using ConcurrentHashMap.
 * Thread-safe implementation for managing user subscription plans.
 * 
 * All users default to FREE plan if no plan is explicitly stored.
 */
@Repository
public class InMemoryPlanRepository implements PlanRepository {
    
    // Store subscription plans per user (thread-safe)
    private final ConcurrentHashMap<String, SubscriptionPlan> userPlans;
    
    public InMemoryPlanRepository() {
        this.userPlans = new ConcurrentHashMap<>();
    }
    
    @Override
    public SubscriptionPlan getPlan(String userId) {
        // Default to FREE plan if no plan is stored
        return userPlans.getOrDefault(userId, SubscriptionPlan.FREE);
    }
    
    @Override
    public void updatePlan(String userId, SubscriptionPlan plan) {
        userPlans.put(userId, plan);
    }
}
