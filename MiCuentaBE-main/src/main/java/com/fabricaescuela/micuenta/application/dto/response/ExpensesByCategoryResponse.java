package com.fabricaescuela.micuenta.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ExpensesByCategoryResponse(
        String period,
        BigDecimal totalExpenses,
        List<CategoryExpense> categories
) {
    public record CategoryExpense(
            Long categoryId,
            String categoryName,
            String color,
            BigDecimal amount,
            BigDecimal percentage
    ) {}
}
