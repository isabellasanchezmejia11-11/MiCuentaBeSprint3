package com.fabricaescuela.micuenta.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SavingGoal(
        Long id,
        Long userId,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        boolean archived,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
