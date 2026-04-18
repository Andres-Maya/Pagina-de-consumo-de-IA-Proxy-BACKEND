package com.aiproxy.application.service;

import com.aiproxy.domain.model.entity.ConsumptionProfile;
import com.aiproxy.domain.model.entity.User;
import com.aiproxy.domain.model.enumeration.SubscriptionTier;
import com.aiproxy.domain.repository.ConsumptionProfileRepository;
import com.aiproxy.domain.repository.UserRepository;
import com.aiproxy.domain.service.SubscriptionManagement;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlanTransitioner implements SubscriptionManagement {

    private final UserRepository userRepository;
    private final ConsumptionProfileRepository consumptionRepository;

    public PlanTransitioner(UserRepository userRepository,
                            ConsumptionProfileRepository consumptionRepository) {
        this.userRepository = userRepository;
        this.consumptionRepository = consumptionRepository;
    }

    @Override
    public void upgradeToPro(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!user.canUpgrade()) {
            throw new IllegalStateException("User cannot upgrade from current tier: " + user.getSubscriptionTier());
        }

        user.upgradeSubscription();
        userRepository.save(user);

        ConsumptionProfile profile = consumptionRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Consumption profile not found"));

        profile.setSubscriptionTier(SubscriptionTier.PRO);
        profile.setTokensRemaining(SubscriptionTier.PRO.getMaxTokensPerMonth());
        profile.setTokensConsumed(0);
        consumptionRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionTier getCurrentTier(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return user.getSubscriptionTier();
    }
}
