package com.fabricaescuela.micuenta.domain.repository;

import java.util.List;
import java.util.Optional;

import com.fabricaescuela.micuenta.domain.model.SavingGoal;

public interface SavingGoalRepository {
    SavingGoal save(SavingGoal savingGoal);

    Optional<SavingGoal> findById(Long id);

    List<SavingGoal> findByUserIdAndArchivedFalse(Long userId);

    List<SavingGoal> findByUserId(Long userId);

    SavingGoal update(SavingGoal savingGoal);

    void deleteById(Long id);

    void archiveById(Long id);
}
