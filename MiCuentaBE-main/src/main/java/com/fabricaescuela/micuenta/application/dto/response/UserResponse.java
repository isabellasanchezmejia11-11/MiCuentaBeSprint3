package com.fabricaescuela.micuenta.application.dto.response;

public record UserResponse(
        Long id,
        String name,
        String lastname,
        String email
) {
}