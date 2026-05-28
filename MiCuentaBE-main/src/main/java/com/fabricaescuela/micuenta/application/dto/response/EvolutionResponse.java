package com.fabricaescuela.micuenta.application.dto.response;



import java.util.List;

public record EvolutionResponse(
        List<MonthlyDataResponse> months
) {
}
