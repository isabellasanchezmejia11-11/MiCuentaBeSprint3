package com.fabricaescuela.micuenta.interfaces.rest;

import com.fabricaescuela.micuenta.application.dto.response.FinancialAdviceResponse;
import com.fabricaescuela.micuenta.application.usecase.GenerateFinancialAdviceUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/ai")
@Tag(name = "Asesor financiero IA", description = "Recomendaciones financieras generadas con Gemini")
@SecurityRequirement(name = "bearerAuth")
public class FinancialAdvisorController {

    private final GenerateFinancialAdviceUseCase generateFinancialAdviceUseCase;

    public FinancialAdvisorController(GenerateFinancialAdviceUseCase generateFinancialAdviceUseCase) {
        this.generateFinancialAdviceUseCase = generateFinancialAdviceUseCase;
    }

    @GetMapping("/financial-advice")
    @Operation(
            summary = "Generar recomendaciones financieras con Gemini",
            description = "Analiza categorías, gastos, presupuestos y movimientos del usuario autenticado para devolver una respuesta estructurada."
    )
    @ApiResponse(responseCode = "200", description = "Recomendación financiera generada")
    public ResponseEntity<FinancialAdviceResponse> getFinancialAdvice(
            @Parameter(description = "Mes (1-12). Si no se proporciona, se usa el mes actual")
            @RequestParam(required = false) Integer month,
            @Parameter(description = "Año (YYYY). Si no se proporciona, se usa el año actual")
            @RequestParam(required = false) Integer year,
            Authentication authentication
    ) {
        LocalDate today = LocalDate.now();
        int resolvedMonth = month != null ? month : today.getMonthValue();
        int resolvedYear = year != null ? year : today.getYear();

        String email = (String) authentication.getPrincipal();
        FinancialAdviceResponse response = generateFinancialAdviceUseCase.execute(email, resolvedMonth, resolvedYear);
        return ResponseEntity.ok(response);
    }
}
