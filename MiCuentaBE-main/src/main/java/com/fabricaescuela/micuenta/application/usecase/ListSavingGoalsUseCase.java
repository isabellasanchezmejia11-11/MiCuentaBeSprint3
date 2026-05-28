package com.fabricaescuela.micuenta.application.usecase;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fabricaescuela.micuenta.application.dto.response.SavingGoalResponse;
import com.fabricaescuela.micuenta.application.exception.ResourceNotFoundException;
import com.fabricaescuela.micuenta.domain.model.SavingGoal;
import com.fabricaescuela.micuenta.domain.model.User;
import com.fabricaescuela.micuenta.domain.repository.SavingGoalRepository;
import com.fabricaescuela.micuenta.domain.repository.UserRepository;

@Service
public class ListSavingGoalsUseCase {

    private final SavingGoalRepository savingGoalRepository;
    private final UserRepository userRepository;

    public ListSavingGoalsUseCase(
            SavingGoalRepository savingGoalRepository,
            UserRepository userRepository
    ) {
        this.savingGoalRepository = savingGoalRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<SavingGoalResponse> execute(String authenticatedEmail) {
        User user = userRepository.findByEmail(authenticatedEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        return savingGoalRepository.findByUserIdAndArchivedFalse(user.id()).stream()
                .map(this::toResponse)
                .toList();
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
