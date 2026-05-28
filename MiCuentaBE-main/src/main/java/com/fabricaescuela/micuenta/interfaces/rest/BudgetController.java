package com.fabricaescuela.micuenta.interfaces.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fabricaescuela.micuenta.application.dto.request.CreateBudgetRequest;
import com.fabricaescuela.micuenta.application.dto.response.BudgetResponse;
import com.fabricaescuela.micuenta.application.dto.response.ErrorResponse;
import com.fabricaescuela.micuenta.application.exception.ResourceNotFoundException;
import com.fabricaescuela.micuenta.application.usecase.CreateBudgetUseCase;
import com.fabricaescuela.micuenta.application.usecase.DeleteBudgetUseCase;
import com.fabricaescuela.micuenta.application.usecase.ListBudgetsUseCase;
import com.fabricaescuela.micuenta.application.usecase.UpdateBudgetUseCase;
import com.fabricaescuela.micuenta.domain.model.Budget;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/presupuestos")
@Tag(name = "Presupuestos", description = "Endpoints para gestionar presupuestos")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {

    private final CreateBudgetUseCase createBudgetUseCase;
    private final ListBudgetsUseCase listBudgetsUseCase;
    private final DeleteBudgetUseCase deleteBudgetUseCase;
    private final UpdateBudgetUseCase updateBudgetUseCase;

    public BudgetController(
            CreateBudgetUseCase createBudgetUseCase,
            ListBudgetsUseCase listBudgetsUseCase,
            DeleteBudgetUseCase deleteBudgetUseCase,
            UpdateBudgetUseCase updateBudgetUseCase) {
        this.createBudgetUseCase = createBudgetUseCase;
        this.listBudgetsUseCase = listBudgetsUseCase;
        this.deleteBudgetUseCase = deleteBudgetUseCase;
        this.updateBudgetUseCase = updateBudgetUseCase;
    }

    @PostMapping
    @Operation(summary = "Crear presupuesto", description = "Crea un nuevo presupuesto para una categoría")
    @ApiResponse(responseCode = "201", description = "Presupuesto creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o presupuesto duplicado")
    public ResponseEntity<BudgetResponse> create(
            @Valid @RequestBody CreateBudgetRequest request,
            Authentication auth
    ) {
        String email = (String) auth.getPrincipal();
        BudgetResponse response = createBudgetUseCase.execute(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar presupuestos", description = "Obtiene todos los presupuestos del usuario autenticado")
    @ApiResponse(responseCode = "200", description = "Lista de presupuestos")
    public ResponseEntity<List<BudgetResponse>> list(Authentication auth) {
        String email = (String) auth.getPrincipal();
        List<BudgetResponse> budgets = listBudgetsUseCase.execute(email);
        return ResponseEntity.ok(budgets);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar presupuesto", description = "Elimina un presupuesto por su ID")
    @ApiResponse(responseCode = "204", description = "Presupuesto eliminado exitosamente")
    @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID del presupuesto")
            @PathVariable Long id,
            Authentication auth
    ) {
        String email = (String) auth.getPrincipal();
        deleteBudgetUseCase.execute(id, email);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar presupuesto", description = "Actualiza un presupuesto existente por su ID")
    @ApiResponse(responseCode = "200", description = "Presupuesto actualizado exitosamente")
    @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado")
    public ResponseEntity<BudgetResponse> update(
            @Parameter(description = "ID del presupuesto")
            @PathVariable Long id,
            @Valid @RequestBody CreateBudgetRequest request
    ) {
        BudgetResponse response = updateBudgetUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }
}
