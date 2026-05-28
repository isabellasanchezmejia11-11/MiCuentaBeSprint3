package com.fabricaescuela.micuenta.application.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateMovementRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,
        @NotNull(message = "Date is required")
        LocalDate date,
        @NotNull(message = "Category ID is required")
        Long categoryId,
        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description
        ) {

}