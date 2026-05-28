package com.fabricaescuela.micuenta.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fabricaescuela.micuenta.application.dto.response.BudgetResponse;
import com.fabricaescuela.micuenta.domain.model.Budget;
import com.fabricaescuela.micuenta.domain.model.Category;
import com.fabricaescuela.micuenta.domain.model.MovementType;
import com.fabricaescuela.micuenta.domain.repository.BudgetRepository;
import com.fabricaescuela.micuenta.domain.repository.CategoryRepository;
import com.fabricaescuela.micuenta.domain.repository.MovementRepository;
import com.fabricaescuela.micuenta.domain.repository.UserRepository;

@Service
public class ListBudgetsUseCase {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final MovementRepository movementRepository;

    public ListBudgetsUseCase(
            BudgetRepository budgetRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            MovementRepository movementRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.movementRepository = movementRepository;
    }

    public List<BudgetResponse> execute(String authenticatedEmail) {
        // Obtener el usuario autenticado
        var user = userRepository.findByEmail(authenticatedEmail.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Obtener todos los presupuestos del usuario
        List<Budget> budgets = budgetRepository.findByUserId(user.id());

        // Convertir a response
        return budgets.stream()
                .map(budget -> {
                    var categoryOpt = categoryRepository.findById(budget.getCategoryId());
                    String categoryName = categoryOpt
                            .map(Category::name)
                            .orElse("Categoría no encontrada");
                    String categoryType = categoryOpt
                            .map(cat -> cat.type().name())
                            .orElse("EXPENSE");

                    // Calcular Valor Ejecutado: suma de gastos en la categoría/mes/año
                    BigDecimal valorEjecutado = calculateExecutedAmount(
                            user.id(),
                            budget.getCategoryId(),
                            budget.getMonth(),
                            budget.getYear()
                    );

                    return new BudgetResponse(
                            budget.getId(),
                            budget.getAmountLimit(),
                            budget.getAlertPercent(),
                            budget.getMonth(),
                            budget.getYear(),
                            budget.getCategoryId(),
                            categoryName,
                            categoryType,
                            budget.getUserId(),
                            valorEjecutado
                    );
                })
                .toList();
    }

    private BigDecimal calculateExecutedAmount(Long userId, Long categoryId, Integer month, Integer year) {
        // Definir rango de fechas del mes
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1); // último día del mes

        return movementRepository.sumAmountByUserIdAndCategoryIdAndDateBetweenAndType(
                userId,
                categoryId,
                startDate,
                endDate,
                MovementType.EXPENSE
        );
    }
}
