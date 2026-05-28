package com.fabricaescuela.micuenta.interfaces.rest;

import java.util.List;
import java.util.stream.Stream;

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

import com.fabricaescuela.micuenta.application.dto.request.CategoryRequest;
import com.fabricaescuela.micuenta.application.dto.response.CategoryResponse;
import com.fabricaescuela.micuenta.application.usecase.CreateCategoryUseCase;
import com.fabricaescuela.micuenta.application.usecase.DeleteCategoryUseCase;
import com.fabricaescuela.micuenta.application.usecase.GetCategoriesUseCase;
import com.fabricaescuela.micuenta.application.usecase.UpdateCategoryUseCase;
import com.fabricaescuela.micuenta.domain.model.Category;
import com.fabricaescuela.micuenta.domain.model.MovementType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/categories")
@Tag(name = "Categorías", description = "Endpoints para gestionar categorías de movimientos")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final GetCategoriesUseCase getCategoriesUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;

    public CategoryController(CreateCategoryUseCase createCategoryUseCase, GetCategoriesUseCase getCategoriesUseCase, DeleteCategoryUseCase deleteCategoryUseCase, UpdateCategoryUseCase updateCategoryUseCase) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.getCategoriesUseCase = getCategoriesUseCase;
        this.deleteCategoryUseCase = deleteCategoryUseCase;
        this.updateCategoryUseCase = updateCategoryUseCase;
    }

    @PostMapping
    @Operation(summary = "Crear nueva categoría", description = "Crea una nueva categoría para el usuario autenticado")
    @ApiResponse(responseCode = "200", description = "Categoría creada exitosamente")
    @ApiResponse(responseCode = "400", description = "Categoría duplicada o datos inválidos")
    public CategoryResponse create(@RequestBody CategoryRequest request, Authentication auth) {
        String email = (String) auth.getPrincipal();
        return createCategoryUseCase.execute(email, request.name(), request.type(), request.color());
    }

    @GetMapping
    @Operation(summary = "Listar categorías", description = "Obtiene las categorías del usuario filtradas por tipo")
    @ApiResponse(responseCode = "200", description = "Lista de categorías")
    public List<CategoryResponse> list(
            @Parameter(description = "Tipo de movimiento (INCOME o EXPENSE, opcional)")
            @RequestParam(required = false) MovementType type,
            Authentication auth
    ) {
        String email = (String) auth.getPrincipal();
        if (type == null) {
            // Si no se especifica tipo, devolver ambos tipos
            List<CategoryResponse> incomes = getCategoriesUseCase.execute(email, MovementType.INCOME).stream()
                    .map(category -> new CategoryResponse(
                            category.id(),
                            category.name(),
                            category.type(),
                            category.userId() != null,
                            category.userId(),
                            category.color()
                    ))
                    .toList();
            List<CategoryResponse> expenses = getCategoriesUseCase.execute(email, MovementType.EXPENSE).stream()
                    .map(category -> new CategoryResponse(
                            category.id(),
                            category.name(),
                            category.type(),
                            category.userId() != null,
                            category.userId(),
                            category.color()
                    ))
                    .toList();
            return Stream.concat(incomes.stream(), expenses.stream()).toList();
        }
        return getCategoriesUseCase.execute(email, type).stream()
                .map(category -> new CategoryResponse(
                        category.id(),
                        category.name(),
                        category.type(),
                        category.userId() != null,
                        category.userId(),
                        category.color()
                ))
                .toList();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría por su ID")
    @ApiResponse(responseCode = "204", description = "Categoría eliminada")
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    public void delete(
            @Parameter(description = "ID de la categoría")
            @PathVariable Long id
    ) {
        deleteCategoryUseCase.execute(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría", description = "Actualiza una categoría existente por su ID")
    @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente")
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o categoría duplicada")
    public CategoryResponse update(
            @Parameter(description = "ID de la categoría")
            @PathVariable Long id,
            @RequestBody CategoryRequest request
    ) {
        return updateCategoryUseCase.execute(id, request);
    }
}