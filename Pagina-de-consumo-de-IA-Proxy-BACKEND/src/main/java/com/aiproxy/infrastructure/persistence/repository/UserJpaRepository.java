package com.aiproxy.infrastructure.persistence.repository;

import com.aiproxy.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, String> {
    Optional<UserJpaEntity> findByUsername(String username);
}
