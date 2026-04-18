package com.aiproxy.infrastructure.persistence.adapter;

import com.aiproxy.domain.model.entity.User;
import com.aiproxy.domain.repository.UserRepository;
import com.aiproxy.infrastructure.persistence.entity.UserJpaEntity;
import com.aiproxy.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        UserJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private User toDomain(UserJpaEntity entity) {
        User user = new User();
        user.setId(entity.getId());
        user.setUsername(entity.getUsername());
        user.setEmail(entity.getEmail());
        user.setSubscriptionTier(entity.getSubscriptionTier());
        user.setCreatedAt(entity.getCreatedAt());
        return user;
    }

    private UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setSubscriptionTier(user.getSubscriptionTier());
        entity.setCreatedAt(user.getCreatedAt());
        return entity;
    }
}
