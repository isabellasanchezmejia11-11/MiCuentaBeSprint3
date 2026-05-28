package com.fabricaescuela.micuenta.application.dto.response;

import java.math.BigDecimal;

public record BudgetResponse(
        Long id,
        BigDecimal amountLimit,
        Integer alertPercent,
        Integer month,
        Integer year,
        Long categoryId,
        String categoryName,
        String categoryType,
        Long userId,
        BigDecimal valorEjecutado
) {
}
