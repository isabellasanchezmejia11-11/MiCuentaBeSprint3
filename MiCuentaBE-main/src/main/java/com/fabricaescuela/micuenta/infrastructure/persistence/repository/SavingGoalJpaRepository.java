package com.fabricaescuela.micuenta.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fabricaescuela.micuenta.infrastructure.persistence.entity.SavingGoalEntity;

public interface SavingGoalJpaRepository extends JpaRepository<SavingGoalEntity, Long> {
    List<SavingGoalEntity> findByUserIdAndArchivedFalse(Long userId);

    List<SavingGoalEntity> findByUserId(Long userId);
}
