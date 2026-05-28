package com.fabricaescuela.micuenta.domain.repository;

import java.util.List;
import java.util.Optional;

import com.fabricaescuela.micuenta.domain.model.SavingGoalContribution;

public interface SavingGoalContributionRepository {
    SavingGoalContribution save(SavingGoalContribution contribution);

    Optional<SavingGoalContribution> findById(Long id);

    List<SavingGoalContribution> findBySavingGoalId(Long savingGoalId);

    void deleteById(Long id);
}
