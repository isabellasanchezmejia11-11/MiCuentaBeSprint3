package com.fabricaescuela.micuenta.domain.model;

public record User(
        Long id,
        String name,
        String lastname,
        String email,
        String passwordHash
) {
}