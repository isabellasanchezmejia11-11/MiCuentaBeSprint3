package com.fabricaescuela.micuenta.application.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateBudgetRequest(

        @NotNull(message = "El monto límite es requerido")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        BigDecimal amountLimit,

        Integer alertPercent,

        @NotNull(message = "El mes es requerido")
        @Min(value = 1, message = "El mes debe estar entre 1 y 12")
        Integer month,

        @NotNull(message = "El año es requerido")
        Integer year,

        @NotNull(message = "La categoría es requerida")
        Long categoryId
) {
}
