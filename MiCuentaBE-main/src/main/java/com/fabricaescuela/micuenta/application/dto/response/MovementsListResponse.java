package com.fabricaescuela.micuenta.application.dto.response;

import java.util.List;

public record MovementsListResponse(
        List<MovementResponse> movements,
        String message
) {
    public static MovementsListResponse withData(List<MovementResponse> movements) {
        return new MovementsListResponse(movements, null);
    }

    public static MovementsListResponse noResults() {
        return new MovementsListResponse(List.of(), "No se encontraron movimientos con esos filtros");
    }
}
