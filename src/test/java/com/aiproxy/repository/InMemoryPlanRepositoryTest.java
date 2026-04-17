package com.aiproxy.repository;

import com.aiproxy.model.SubscriptionPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InMemoryPlanRepository.
 * Validates plan storage, retrieval, and default behavior.
 */
class InMemoryPlanRepositoryTest {
    
    private InMemoryPlanRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new InMemoryPlanRepository();
    }
    
    @Test
    void getPlan_shouldReturnFreeByDefault() {
        // When: Getting plan for a new user
        SubscriptionPlan plan = repository.getPlan("user1");
        
        // Then: Should return FREE plan
        assertEquals(SubscriptionPlan.FREE, plan);
    }
    
    @Test
    void updatePlan_shouldStorePlan() {
        // Given: A user ID
        String userId = "user1";
        
        // When: Updating to PRO plan
        repository.updatePlan(userId, SubscriptionPlan.PRO);
        
        // Then: Should retrieve PRO plan
        assertEquals(SubscriptionPlan.PRO, repository.getPlan(userId));
    }
    
    @Test
    void updatePlan_shouldOverwriteExistingPlan() {
        // Given: A user with FREE plan
        String userId = "user1";
        repository.updatePlan(userId, SubscriptionPlan.FREE);
        
        // When: Upgrading to ENTERPRISE
        repository.updatePlan(userId, SubscriptionPlan.ENTERPRISE);
        
        // Then: Should retrieve ENTERPRISE plan
        assertEquals(SubscriptionPlan.ENTERPRISE, repository.getPlan(userId));
    }
    
    @Test
    void getPlan_shouldHandleMultipleUsers() {
        // Given: Multiple users with different plans
        repository.updatePlan("user1", SubscriptionPlan.FREE);
        repository.updatePlan("user2", SubscriptionPlan.PRO);
        repository.updatePlan("user3", SubscriptionPlan.ENTERPRISE);
        
        // When/Then: Each user should have their own plan
        assertEquals(SubscriptionPlan.FREE, repository.getPlan("user1"));
        assertEquals(SubscriptionPlan.PRO, repository.getPlan("user2"));
        assertEquals(SubscriptionPlan.ENTERPRISE, repository.getPlan("user3"));
    }
    
    @Test
    void getPlan_shouldReturnFreeForUnknownUser() {
        // Given: Some users with plans
        repository.updatePlan("user1", SubscriptionPlan.PRO);
        
        // When: Getting plan for a different user
        SubscriptionPlan plan = repository.getPlan("user2");
        
        // Then: Should return FREE plan
        assertEquals(SubscriptionPlan.FREE, plan);
    }
}
