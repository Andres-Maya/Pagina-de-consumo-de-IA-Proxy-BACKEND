package com.aiproxy.domain.repository;

import com.aiproxy.domain.model.entity.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String id);
    Optional<User> findByUsername(String username);
    User save(User user);
}
