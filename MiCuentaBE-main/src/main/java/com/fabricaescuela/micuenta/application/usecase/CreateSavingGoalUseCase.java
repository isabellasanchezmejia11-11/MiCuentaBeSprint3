package com.fabricaescuela.micuenta.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fabricaescuela.micuenta.application.dto.request.CreateSavingGoalRequest;
import com.fabricaescuela.micuenta.application.dto.response.SavingGoalResponse;
import com.fabricaescuela.micuenta.application.exception.ResourceNotFoundException;
import com.fabricaescuela.micuenta.domain.model.SavingGoal;
import com.fabricaescuela.micuenta.domain.model.User;
import com.fabricaescuela.micuenta.domain.repository.SavingGoalRepository;
import com.fabricaescuela.micuenta.domain.repository.UserRepository;

@Service
public class CreateSavingGoalUseCase {

    private final SavingGoalRepository savingGoalRepository;
    private final UserRepository userRepository;

    public CreateSavingGoalUseCase(
            SavingGoalRepository savingGoalRepository,
            UserRepository userRepository
    ) {
        this.savingGoalRepository = savingGoalRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SavingGoalResponse execute(String authenticatedEmail, CreateSavingGoalRequest request) {
        User user = userRepository.findByEmail(authenticatedEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        LocalDateTime now = LocalDateTime.now();
        SavingGoal savingGoal = new SavingGoal(
                null,
                user.id(),
                request.name(),
                request.targetAmount(),
                BigDecimal.ZERO,
                false,
                now,
                now
        );

        SavingGoal saved = savingGoalRepository.save(savingGoal);
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
