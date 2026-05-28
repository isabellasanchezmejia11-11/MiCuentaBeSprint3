package com.fabricaescuela.micuenta.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;


import com.fabricaescuela.micuenta.domain.model.MovementType;

public record MovementResponse(
        Long id,
        Long categoryId,
        BigDecimal amount,
        LocalDate date,
        MovementType type,
        String categoryName,
        String description
) {
}