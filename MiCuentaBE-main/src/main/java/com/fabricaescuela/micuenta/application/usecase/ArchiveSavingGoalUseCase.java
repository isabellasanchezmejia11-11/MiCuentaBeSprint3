package com.fabricaescuela.micuenta.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fabricaescuela.micuenta.application.exception.ResourceNotFoundException;
import com.fabricaescuela.micuenta.domain.repository.SavingGoalRepository;

@Service
public class ArchiveSavingGoalUseCase {

    private final SavingGoalRepository savingGoalRepository;

    public ArchiveSavingGoalUseCase(SavingGoalRepository savingGoalRepository) {
        this.savingGoalRepository = savingGoalRepository;
    }

    @Transactional
    public void execute(Long savingGoalId) {
        savingGoalRepository.findById(savingGoalId)
                .orElseThrow(() -> new ResourceNotFoundException("Saving goal not found"));
        
        savingGoalRepository.archiveById(savingGoalId);
    }
}
