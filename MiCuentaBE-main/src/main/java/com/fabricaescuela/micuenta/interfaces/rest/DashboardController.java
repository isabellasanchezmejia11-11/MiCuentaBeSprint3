package com.fabricaescuela.micuenta.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fabricaescuela.micuenta.application.dto.response.DashboardSummaryResponse;
import com.fabricaescuela.micuenta.application.dto.response.EvolutionResponse;
import com.fabricaescuela.micuenta.application.dto.response.ExpensesByCategoryResponse;
import com.fabricaescuela.micuenta.application.usecase.GetMonthlyDashboardSummaryUseCase;
import com.fabricaescuela.micuenta.application.usecase.GetMonthlyEvolutionUseCase;
import com.fabricaescuela.micuenta.application.usecase.GetExpensesByCategoryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Endpoints para obtener resúmenes y análisis")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final GetMonthlyDashboardSummaryUseCase getMonthlyDashboardSummaryUseCase;
    private final GetMonthlyEvolutionUseCase getMonthlyEvolutionUseCase;
    private final GetExpensesByCategoryUseCase getExpensesByCategoryUseCase;

    public DashboardController(
            GetMonthlyDashboardSummaryUseCase getMonthlyDashboardSummaryUseCase,
            GetMonthlyEvolutionUseCase getMonthlyEvolutionUseCase,
            GetExpensesByCategoryUseCase getExpensesByCategoryUseCase
    ) {
        this.getMonthlyDashboardSummaryUseCase = getMonthlyDashboardSummaryUseCase;
        this.getMonthlyEvolutionUseCase = getMonthlyEvolutionUseCase;
        this.getExpensesByCategoryUseCase = getExpensesByCategoryUseCase;
    }

    @GetMapping("/monthly-summary")
    @Operation(summary = "Obtener resumen mensual", description = "Retorna un resumen de ingresos, gastos y presupuestos del mes actual")
    @ApiResponse(responseCode = "200", description = "Resumen mensual")
    public ResponseEntity<DashboardSummaryResponse> getMonthlySummary(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        DashboardSummaryResponse response = getMonthlyDashboardSummaryUseCase.execute(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/evolution")
    @Operation(summary = "Obtener evolución mensual", description = "Retorna la evolución de ingresos y gastos de los últimos 6 meses en formato de gráfico o tabla")
    @ApiResponse(responseCode = "200", description = "Evolución mensual de ingresos y gastos")
    public ResponseEntity<EvolutionResponse> getMonthlyEvolution(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        EvolutionResponse response = getMonthlyEvolutionUseCase.execute(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expenses-by-category")
    @Operation(summary = "Obtener gastos por categoría", description = "Retorna la distribución de gastos por categoría para un mes específico con montos absolutos y porcentajes")
    @ApiResponse(responseCode = "200", description = "Distribución de gastos por categoría")
    public ResponseEntity<ExpensesByCategoryResponse> getExpensesByCategory(
            @Parameter(description = "Mes (1-12). Si no se proporciona, se usa el mes actual")
            @RequestParam(required = false) Integer month,
            @Parameter(description = "Año (YYYY). Si no se proporciona, se usa el año actual")
            @RequestParam(required = false) Integer year,
            Authentication authentication
    ) {
        String email = (String) authentication.getPrincipal();
        
        // Si no se especifica mes/año, usar el mes actual
        if (month == null || year == null) {
            java.time.LocalDate today = java.time.LocalDate.now();
            if (month == null) month = today.getMonthValue();
            if (year == null) year = today.getYear();
        }
        
        ExpensesByCategoryResponse response = getExpensesByCategoryUseCase.execute(email, month, year);
        return ResponseEntity.ok(response);
    }
}