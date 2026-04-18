package com.aiproxy.infrastructure.persistence.repository;

import com.aiproxy.infrastructure.persistence.entity.DailyUsageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyUsageJpaRepository extends JpaRepository<DailyUsageJpaEntity, String> {
    Optional<DailyUsageJpaEntity> findByUserIdAndDate(String userId, LocalDate date);
    List<DailyUsageJpaEntity> findByUserIdAndDateBetween(String userId, LocalDate start, LocalDate end);
}
