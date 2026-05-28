package com.fabricaescuela.micuenta.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fabricaescuela.micuenta.infrastructure.persistence.entity.SavingGoalContributionEntity;

public interface SavingGoalContributionJpaRepository extends JpaRepository<SavingGoalContributionEntity, Long> {
    List<SavingGoalContributionEntity> findBySavingGoalId(Long savingGoalId);
}
