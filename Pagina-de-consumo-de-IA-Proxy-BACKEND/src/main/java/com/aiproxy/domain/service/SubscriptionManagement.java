package com.aiproxy.domain.service;

import com.aiproxy.domain.model.enumeration.SubscriptionTier;

public interface SubscriptionManagement {
    void upgradeToPro(String userId);
    SubscriptionTier getCurrentTier(String userId);
}
