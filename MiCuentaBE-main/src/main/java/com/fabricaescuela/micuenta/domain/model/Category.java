package com.fabricaescuela.micuenta.domain.model;

public record Category(
        Long id,
        String name,
        MovementType type,
        Long userId,
        String color
) {}