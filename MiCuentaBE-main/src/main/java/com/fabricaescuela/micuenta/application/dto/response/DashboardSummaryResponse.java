package com.fabricaescuela.micuenta.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
        String month,
        BigDecimal monthlyIncome,
        BigDecimal monthlyExpense,
        BigDecimal monthlyNet,
        BigDecimal currentBalance,
        List<MovementResponse> monthlyMovements
) {
}