package com.fabricaescuela.micuenta.application.usecase;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fabricaescuela.micuenta.application.dto.request.AddContributionRequest;
import com.fabricaescuela.micuenta.application.dto.response.SavingGoalResponse;
import com.fabricaescuela.micuenta.application.exception.ResourceNotFoundException;
import com.fabricaescuela.micuenta.domain.model.SavingGoal;
import com.fabricaescuela.micuenta.domain.model.SavingGoalContribution;
import com.fabricaescuela.micuenta.domain.repository.SavingGoalContributionRepository;
import com.fabricaescuela.micuenta.domain.repository.SavingGoalRepository;
import java.time.LocalDateTime;

@Service
public class AddContributionUseCase {

    private final SavingGoalRepository savingGoalRepository;
    private final SavingGoalContributionRepository contributionRepository;

    public AddContributionUseCase(
            SavingGoalRepository savingGoalRepository,
            SavingGoalContributionRepository contributionRepository
    ) {
        this.savingGoalRepository = savingGoalRepository;
        this.contributionRepository = contributionRepository;
    }

    @Transactional
    public SavingGoalResponse execute(Long savingGoalId, AddContributionRequest request) {
        SavingGoal goal = savingGoalRepository.findById(savingGoalId)
                .orElseThrow(() -> new ResourceNotFoundException("Saving goal not found"));

        // Registrar el abono
        SavingGoalContribution contribution = new SavingGoalContribution(
                null,
                savingGoalId,
                request.amount(),
                request.description(),
                LocalDateTime.now()
        );
        contributionRepository.save(contribution);

        // Actualizar el monto actual de la meta
        BigDecimal newAmount = goal.currentAmount().add(request.amount());
        SavingGoal updatedGoal = new SavingGoal(
                goal.id(),
                goal.userId(),
                goal.name(),
                goal.targetAmount(),
                newAmount,
                goal.archived(),
                goal.createdAt(),
                LocalDateTime.now()
        );
        SavingGoal saved = savingGoalRepository.update(updatedGoal);
        return toResponse(saved);
    }

    private SavingGoalResponse toResponse(SavingGoal goal) {
        BigDecimal percentage = calculatePercentage(goal.currentAmount(), goal.targetAmount());
        String status = getStatus(percentage);
        String motivationMessage = getMotivationMessage(goal.name(), percentage);

        return new SavingGoalResponse(
                goal.id(),
                goal.name(),
                goal.targetAmount(),
                goal.currentAmount(),
                percentage,
                status,
                motivationMessage,
                goal.archived()
        );
    }

    private BigDecimal calculatePercentage(BigDecimal current, BigDecimal target) {
        if (target.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return current.divide(target, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String getStatus(BigDecimal percentage) {
        if (percentage.compareTo(new BigDecimal("100")) >= 0) {
            return "COMPLETED";
        } else if (percentage.compareTo(new BigDecimal("50")) >= 0) {
            return "HALFWAY";
        }
        return "IN_PROGRESS";
    }

    private String getMotivationMessage(String goalName, BigDecimal percentage) {
        if (percentage.compareTo(new BigDecimal("100")) >= 0) {
            return "¡Meta alcanzada! Has completado tu objetivo: " + goalName;
        } else if (percentage.compareTo(new BigDecimal("50")) >= 0) {
            return "¡Vas a mitad de camino para tu meta " + goalName + "!";
        }
        return null;
    }
}
