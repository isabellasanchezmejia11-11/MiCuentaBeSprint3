package com.fabricaescuela.micuenta.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record Movement(
        Long id,
        Long userId,
        BigDecimal amount,
        LocalDate date,
        MovementType type,
        Long categoryId,
        String description,
        LocalDateTime createdAt,
        LocalDateTime lastModifiedAt
) {
}