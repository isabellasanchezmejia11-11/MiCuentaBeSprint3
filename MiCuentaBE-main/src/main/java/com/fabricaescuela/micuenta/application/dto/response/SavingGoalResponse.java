package com.fabricaescuela.micuenta.application.dto.response;

import java.math.BigDecimal;

public record SavingGoalResponse(
        Long id,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        BigDecimal percentageProgress,
        String status,
        String motivationMessage,
        boolean archived
) {}
