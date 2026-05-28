package com.fabricaescuela.micuenta.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SavingGoalContribution(
        Long id,
        Long savingGoalId,
        BigDecimal amount,
        String description,
        LocalDateTime createdAt
) {}
