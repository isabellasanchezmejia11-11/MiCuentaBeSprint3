package com.fabricaescuela.micuenta.application.dto.response;

import com.fabricaescuela.micuenta.domain.model.MovementType;

public record CategoryResponse(
        Long id,
        String name,
        MovementType type,
        boolean personal,
        Long userId,
        String color
) {
}
