package com.fabricaescuela.micuenta.interfaces.rest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fabricaescuela.micuenta.application.dto.request.CreateMovementRequest;
import com.fabricaescuela.micuenta.application.dto.request.UpdateMovementRequest;
import com.fabricaescuela.micuenta.application.dto.response.MovementResponse;
import com.fabricaescuela.micuenta.application.dto.response.MovementsListResponse;
import com.fabricaescuela.micuenta.application.usecase.DeleteMovementUseCase;
import com.fabricaescuela.micuenta.application.usecase.ListExpensesUseCase;
import com.fabricaescuela.micuenta.application.usecase.ListIncomesUseCase;
import com.fabricaescuela.micuenta.application.usecase.ListMovementsUseCase;
import com.fabricaescuela.micuenta.application.usecase.RegisterExpenseUseCase;
import com.fabricaescuela.micuenta.application.usecase.RegisterIncomeUseCase;
import com.fabricaescuela.micuenta.application.usecase.UpdateMovementUseCase;
import com.fabricaescuela.micuenta.domain.model.MovementType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/movements")
@Tag(name = "Movimientos", description = "Endpoints para gestionar ingresos y gastos")
@SecurityRequirement(name = "bearerAuth")
public class MovementController {

    private final RegisterIncomeUseCase registerIncomeUseCase;
    private final RegisterExpenseUseCase registerExpenseUseCase;
    private final UpdateMovementUseCase updateMovementUseCase;
    private final DeleteMovementUseCase deleteMovementUseCase;
    private final ListIncomesUseCase listIncomesUseCase;
    private final ListExpensesUseCase listExpensesUseCase;
    private final ListMovementsUseCase listMovementsUseCase;

    public MovementController(
            RegisterIncomeUseCase registerIncomeUseCase,
            RegisterExpenseUseCase registerExpenseUseCase,
            UpdateMovementUseCase updateMovementUseCase,
            DeleteMovementUseCase deleteMovementUseCase,
            ListIncomesUseCase listIncomesUseCase,
            ListExpensesUseCase listExpensesUseCase,
            ListMovementsUseCase listMovementsUseCase
    ) {
        this.registerIncomeUseCase = registerIncomeUseCase;
        this.registerExpenseUseCase = registerExpenseUseCase;
        this.updateMovementUseCase = updateMovementUseCase;
        this.deleteMovementUseCase = deleteMovementUseCase;
        this.listIncomesUseCase = listIncomesUseCase;
        this.listExpensesUseCase = listExpensesUseCase;
        this.listMovementsUseCase = listMovementsUseCase;
    }

    @PostMapping("/incomes")
    @Operation(summary = "Registrar ingreso", description = "Crea un nuevo movimiento de ingreso")
    @ApiResponse(responseCode = "201", description = "Ingreso registrado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    public ResponseEntity<MovementResponse> registerIncome(
            @Valid @RequestBody CreateMovementRequest request,
            Authentication authentication
    ) {
        String email = (String) authentication.getPrincipal();
        MovementResponse response = registerIncomeUseCase.execute(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/expenses")
    @Operation(summary = "Registrar gasto", description = "Crea un nuevo movimiento de gasto")
    @ApiResponse(responseCode = "201", description = "Gasto registrado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    public ResponseEntity<MovementResponse> registerExpense(
            @Valid @RequestBody CreateMovementRequest request,
            Authentication authentication
    ) {
        String email = (String) authentication.getPrincipal();
        MovementResponse response = registerExpenseUseCase.execute(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/incomes")
    @Operation(summary = "Listar ingresos", description = "Obtiene todos los ingresos del usuario")
    @ApiResponse(responseCode = "200", description = "Lista de ingresos")
    public ResponseEntity<List<MovementResponse>> listIncomes(
            @Parameter(description = "Fecha de inicio (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Fecha de fin (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(listIncomesUseCase.execute(email, Optional.ofNullable(startDate), Optional.ofNullable(endDate)));
    }

    @GetMapping("/expenses")
    @Operation(summary = "Listar gastos", description = "Obtiene todos los gastos del usuario")
    @ApiResponse(responseCode = "200", description = "Lista de gastos")
    public ResponseEntity<List<MovementResponse>> listExpenses(
            @Parameter(description = "Fecha de inicio (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Fecha de fin (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(listExpensesUseCase.execute(email, Optional.ofNullable(startDate), Optional.ofNullable(endDate)));
    }

    @GetMapping
    @Operation(summary = "Listar movimientos", description = "Obtiene movimientos del usuario con filtros opcionales")
    @ApiResponse(responseCode = "200", description = "Lista de movimientos")
    public ResponseEntity<MovementsListResponse> listMovements(
            @Parameter(description = "Tipo de movimiento (INCOME o EXPENSE)")
            @RequestParam(required = false) MovementType type,
            @Parameter(description = "ID de la categoría")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Fecha de inicio (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Fecha de fin (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        String email = (String) authentication.getPrincipal();
        List<MovementResponse> movements = listMovementsUseCase.execute(email, type, categoryId, startDate, endDate);

        if (movements.isEmpty()) {
            return ResponseEntity.ok(MovementsListResponse.noResults());
        }

        return ResponseEntity.ok(MovementsListResponse.withData(movements));
    }

    @PutMapping("/{movementId}")
    @Operation(summary = "Actualizar movimiento", description = "Actualiza los datos de un movimiento existente")
    @ApiResponse(responseCode = "200", description = "Movimiento actualizado")
    @ApiResponse(responseCode = "404", description = "Movimiento no encontrado")
    public ResponseEntity<MovementResponse> updateMovement(
            @Parameter(description = "ID del movimiento")
            @PathVariable Long movementId,
            @Valid @RequestBody UpdateMovementRequest request,
            Authentication authentication
    ) {
        String email = (String) authentication.getPrincipal();
        MovementResponse response = updateMovementUseCase.execute(movementId, email, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{movementId}")
    @Operation(summary = "Eliminar movimiento", description = "Elimina un movimiento por su ID")
    @ApiResponse(responseCode = "204", description = "Movimiento eliminado")
    @ApiResponse(responseCode = "404", description = "Movimiento no encontrado")
    public ResponseEntity<Void> deleteMovement(
            @Parameter(description = "ID del movimiento")
            @PathVariable Long movementId,
            Authentication authentication
    ) {
        String email = (String) authentication.getPrincipal();
        deleteMovementUseCase.execute(movementId, email);
        return ResponseEntity.noContent().build();
    }
}