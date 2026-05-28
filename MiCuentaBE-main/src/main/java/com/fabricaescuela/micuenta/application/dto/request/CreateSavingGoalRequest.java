package com.fabricaescuela.micuenta.application.dto.request;

import java.math.BigDecimal;

public record CreateSavingGoalRequest(
        String name,
        BigDecimal targetAmount
) {}
