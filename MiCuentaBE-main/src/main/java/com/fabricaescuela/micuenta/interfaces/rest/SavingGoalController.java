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

import com.fabricaescuela.micuenta.application.dto.request.AddContributionRequest;
import com.fabricaescuela.micuenta.application.dto.request.CreateSavingGoalRequest;
import com.fabricaescuela.micuenta.application.dto.response.SavingGoalResponse;
import com.fabricaescuela.micuenta.application.usecase.AddContributionUseCase;
import com.fabricaescuela.micuenta.application.usecase.ArchiveSavingGoalUseCase;
import com.fabricaescuela.micuenta.application.usecase.CreateSavingGoalUseCase;
import com.fabricaescuela.micuenta.application.usecase.DeleteSavingGoalUseCase;
import com.fabricaescuela.micuenta.application.usecase.ListSavingGoalsUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/saving-goals")
@Tag(name = "Metas de Ahorro", description = "Endpoints para gestionar metas de ahorro")
@SecurityRequirement(name = "bearerAuth")
public class SavingGoalController {

    private final CreateSavingGoalUseCase createSavingGoalUseCase;
    private final ListSavingGoalsUseCase listSavingGoalsUseCase;
    private final AddContributionUseCase addContributionUseCase;
    private final ArchiveSavingGoalUseCase archiveSavingGoalUseCase;
    private final DeleteSavingGoalUseCase deleteSavingGoalUseCase;

    public SavingGoalController(
            CreateSavingGoalUseCase createSavingGoalUseCase,
            ListSavingGoalsUseCase listSavingGoalsUseCase,
            AddContributionUseCase addContributionUseCase,
            ArchiveSavingGoalUseCase archiveSavingGoalUseCase,
            DeleteSavingGoalUseCase deleteSavingGoalUseCase
    ) {
        this.createSavingGoalUseCase = createSavingGoalUseCase;
        this.listSavingGoalsUseCase = listSavingGoalsUseCase;
        this.addContributionUseCase = addContributionUseCase;
        this.archiveSavingGoalUseCase = archiveSavingGoalUseCase;
        this.deleteSavingGoalUseCase = deleteSavingGoalUseCase;
    }

    @PostMapping
    @Operation(summary = "Crear una meta de ahorro", description = "Crea una nueva meta de ahorro con nombre y monto objetivo")
    @ApiResponse(responseCode = "201", description = "Meta de ahorro creada exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    public ResponseEntity<SavingGoalResponse> createSavingGoal(
            @Valid @RequestBody CreateSavingGoalRequest request,
            Authentication authentication
    ) {
        String email = (String) authentication.getPrincipal();
        SavingGoalResponse response = createSavingGoalUseCase.execute(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar metas de ahorro", description = "Obtiene todas las metas de ahorro activas del usuario")
    @ApiResponse(responseCode = "200", description = "Lista de metas de ahorro")
    public ResponseEntity<List<SavingGoalResponse>> listSavingGoals(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        List<SavingGoalResponse> response = listSavingGoalsUseCase.execute(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/contributions")
    @Operation(summary = "Agregar abono a una meta", description = "Registra un abono hacia una meta de ahorro y actualiza el progreso")
    @ApiResponse(responseCode = "200", description = "Abono registrado y meta actualizada")
    @ApiResponse(responseCode = "404", description = "Meta de ahorro no encontrada")
    public ResponseEntity<SavingGoalResponse> addContribution(
            @Parameter(description = "ID de la meta de ahorro")
            @PathVariable Long id,
            @Valid @RequestBody AddContributionRequest request,
            Authentication authentication
    ) {
        SavingGoalResponse response = addContributionUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/archive")
    @Operation(summary = "Archivar una meta", description = "Archiva una meta de ahorro completada")
    @ApiResponse(responseCode = "204", description = "Meta archivada exitosamente")
    @ApiResponse(responseCode = "404", description = "Meta de ahorro no encontrada")
    public ResponseEntity<Void> archiveSavingGoal(
            @Parameter(description = "ID de la meta de ahorro")
            @PathVariable Long id,
            Authentication authentication
    ) {
        archiveSavingGoalUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una meta", description = "Elimina una meta de ahorro")
    @ApiResponse(responseCode = "204", description = "Meta eliminada exitosamente")
    @ApiResponse(responseCode = "404", description = "Meta de ahorro no encontrada")
    public ResponseEntity<Void> deleteSavingGoal(
            @Parameter(description = "ID de la meta de ahorro")
            @PathVariable Long id,
            Authentication authentication
    ) {
        deleteSavingGoalUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
