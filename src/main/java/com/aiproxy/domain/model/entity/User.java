package com.aiproxy.domain.model.entity;

import com.aiproxy.domain.model.enumeration.SubscriptionTier;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private String id;
    private String username;
    private String email;
    private SubscriptionTier subscriptionTier;
    private LocalDateTime createdAt;

    public User() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.subscriptionTier = SubscriptionTier.FREE;
    }

    public User(String username, String email) {
        this();
        this.username = username;
        this.email = email;
    }

    public void upgradeSubscription() {
        if (this.subscriptionTier == SubscriptionTier.FREE) {
            this.subscriptionTier = SubscriptionTier.PRO;
        }
    }

    public boolean canUpgrade() {
        return this.subscriptionTier == SubscriptionTier.FREE;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public SubscriptionTier getSubscriptionTier() { return subscriptionTier; }
    public void setSubscriptionTier(SubscriptionTier subscriptionTier) { this.subscriptionTier = subscriptionTier; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
