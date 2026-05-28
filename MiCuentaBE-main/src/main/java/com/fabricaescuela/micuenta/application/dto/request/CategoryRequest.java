package com.fabricaescuela.micuenta.application.dto.request;

import com.fabricaescuela.micuenta.domain.model.MovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotNull(message = "Name is required")
        @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
        String name,

        @NotNull(message = "Type is required")
        MovementType type,

        String color
) {
}
