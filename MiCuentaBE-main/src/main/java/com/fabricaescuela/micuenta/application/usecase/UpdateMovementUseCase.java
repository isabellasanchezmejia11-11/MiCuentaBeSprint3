package com.fabricaescuela.micuenta.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.fabricaescuela.micuenta.application.dto.request.UpdateMovementRequest;
import com.fabricaescuela.micuenta.application.dto.response.MovementResponse;
import com.fabricaescuela.micuenta.application.exception.ResourceNotFoundException;
import com.fabricaescuela.micuenta.domain.model.Category;
import com.fabricaescuela.micuenta.domain.model.Movement;
import com.fabricaescuela.micuenta.domain.model.MovementType;
import com.fabricaescuela.micuenta.domain.model.User;
import com.fabricaescuela.micuenta.domain.repository.BudgetRepository;
import com.fabricaescuela.micuenta.domain.repository.CategoryRepository;
import com.fabricaescuela.micuenta.domain.repository.MovementRepository;
import com.fabricaescuela.micuenta.domain.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UpdateMovementUseCase {

    private final MovementRepository movementRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;

    public UpdateMovementUseCase(
            MovementRepository movementRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            BudgetRepository budgetRepository) {
        this.movementRepository = movementRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.budgetRepository = budgetRepository;
    }

    @Transactional
    public MovementResponse execute(Long movementId, String authenticatedEmail,
            UpdateMovementRequest request) {

        // Validar usuario autenticado
        User user = userRepository.findByEmail(authenticatedEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        // Obtener movimiento existente
        Movement existing = movementRepository.findById(movementId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Movement not found with id: " + movementId));

        // Validar pertenencia del movimiento al usuario
        if (!existing.userId().equals(user.id())) {
            throw new ResourceNotFoundException(
                    "Movement not found with id: " + movementId);
        }

        // Validar que la categoría existe
        categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.categoryId()));

        // Crear movimiento actualizado
        Movement updated = new Movement(
                existing.id(),
                existing.userId(),
                request.amount(),
                request.date(),
                existing.type(), // El tipo NO puede cambiar
                request.categoryId(),
                normalizeDescription(request.description()),
                existing.createdAt(),
                LocalDateTime.now() // Registrar fecha de modificación
        );

        // Guardar movimiento actualizado
        Movement saved = movementRepository.update(updated);

        // Recalcular presupuesto si es gasto y la categoría cambió o el monto cambió
        if (saved.type() == MovementType.EXPENSE) {
            recalculateBudgetIfNeeded(existing, saved, user.id());
        }

        return toResponse(saved);
    }

    /**
     * Recalcula el presupuesto del mes/año si el monto o categoría cambió
     */
    private void recalculateBudgetIfNeeded(Movement existing, Movement updated, Long userId) {
        boolean categoryChanged = !existing.categoryId().equals(updated.categoryId());
        boolean amountChanged = existing.amount().compareTo(updated.amount()) != 0;

        if (!categoryChanged && !amountChanged) {
            return; // No hay cambios relevantes para el presupuesto
        }

        java.time.YearMonth yearMonth = java.time.YearMonth.from(updated.date());
        int month = yearMonth.getMonthValue();
        int year = yearMonth.getYear();

        // Si cambió la categoría, recalcular presupuesto de ambas categorías
        if (categoryChanged) {
            budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                    userId, existing.categoryId(), month, year)
                    .ifPresent(budget -> {
                        // El presupuesto se recalcula automáticamente en la consulta de gastos
                        // pero aquí podríamos trigger un evento o validación adicional
                    });
        }

        // Recalcular presupuesto de la categoría actual
        budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                userId, updated.categoryId(), month, year)
                .ifPresent(budget -> {
                    // El presupuesto se recalcula automáticamente en la consulta de gastos
                    // pero aquí podríamos trigger un evento o validación adicional
                });
    }

    private String normalizeDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }
        return description.trim();
    }

    private MovementResponse toResponse(Movement movement) {
        String categoryName = categoryRepository.findById(movement.categoryId())
                .map(Category::name)
                .orElse("Sin categoría");

        return new MovementResponse(
                movement.id(),
                movement.categoryId(),
                movement.amount(),
                movement.date(),
                movement.type(),
                categoryName,
                movement.description()
        );
    }
}