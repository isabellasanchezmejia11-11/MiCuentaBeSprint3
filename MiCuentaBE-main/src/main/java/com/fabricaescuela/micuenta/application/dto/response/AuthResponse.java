package com.fabricaescuela.micuenta.application.dto.response;

public record AuthResponse(
        String token,
        String type
) {
}
