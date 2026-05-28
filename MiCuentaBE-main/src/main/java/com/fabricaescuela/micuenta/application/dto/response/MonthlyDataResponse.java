package com.fabricaescuela.micuenta.application.dto.response;

import java.math.BigDecimal;

public record MonthlyDataResponse(
        String month,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {
}
