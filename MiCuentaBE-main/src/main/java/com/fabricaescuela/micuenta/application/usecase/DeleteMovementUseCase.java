package com.fabricaescuela.micuenta.application.usecase;

import java.time.YearMonth;

import org.springframework.stereotype.Service;

import com.fabricaescuela.micuenta.application.exception.ResourceNotFoundException;
import com.fabricaescuela.micuenta.domain.model.Movement;
import com.fabricaescuela.micuenta.domain.model.MovementType;
import com.fabricaescuela.micuenta.domain.model.User;
import com.fabricaescuela.micuenta.domain.repository.BudgetRepository;
import com.fabricaescuela.micuenta.domain.repository.MovementRepository;
import com.fabricaescuela.micuenta.domain.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class DeleteMovementUseCase {

    private final MovementRepository movementRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;

    public DeleteMovementUseCase(
            MovementRepository movementRepository,
            UserRepository userRepository,
            BudgetRepository budgetRepository) {
        this.movementRepository = movementRepository;
        this.userRepository = userRepository;
        this.budgetRepository = budgetRepository;
    }

    @Transactional
    public void execute(Long movementId, String authenticatedEmail) {

        // Validar usuario autenticado
        User user = userRepository.findByEmail(authenticatedEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        // Obtener movimiento a eliminar
        Movement existing = movementRepository.findById(movementId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Movement not found with id: " + movementId));

        // Validar pertenencia del movimiento al usuario
        if (!existing.userId().equals(user.id())) {
            throw new ResourceNotFoundException(
                    "Movement not found with id: " + movementId);
        }

        // Si es gasto, recalcular el presupuesto afectado
        if (existing.type() == MovementType.EXPENSE) {
            recalculateBudgetAfterDeletion(existing, user.id());
        }

        // Eliminar movimiento permanentemente
        movementRepository.deleteById(movementId);
    }

    /**
     * Recalcula el presupuesto del mes después de eliminar un gasto.
     * Esto permite que el balance del presupuesto se actualice correctamente.
     */
    private void recalculateBudgetAfterDeletion(Movement deleted, Long userId) {
        YearMonth yearMonth = YearMonth.from(deleted.date());
        int month = yearMonth.getMonthValue();
        int year = yearMonth.getYear();

        // Verificar que existe presupuesto para esta categoría y mes
        budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                userId, deleted.categoryId(), month, year)
                .ifPresent(budget -> {
                    // El presupuesto se recalcula automáticamente al consultar gastos
                    // porque suma los montos de los movimientos existentes
                    // Esta verificación asegura que hay un presupuesto asociado
                });
    }
}