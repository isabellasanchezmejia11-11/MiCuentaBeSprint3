package com.fabricaescuela.micuenta.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fabricaescuela.micuenta.application.dto.response.ExpensesByCategoryResponse;
import com.fabricaescuela.micuenta.application.dto.response.ExpensesByCategoryResponse.CategoryExpense;
import com.fabricaescuela.micuenta.application.exception.ResourceNotFoundException;
import com.fabricaescuela.micuenta.domain.model.Category;
import com.fabricaescuela.micuenta.domain.model.Movement;
import com.fabricaescuela.micuenta.domain.model.MovementType;
import com.fabricaescuela.micuenta.domain.model.User;
import com.fabricaescuela.micuenta.domain.repository.CategoryRepository;
import com.fabricaescuela.micuenta.domain.repository.MovementRepository;
import com.fabricaescuela.micuenta.domain.repository.UserRepository;

@Service
public class GetExpensesByCategoryUseCase {

    private final MovementRepository movementRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public GetExpensesByCategoryUseCase(
            MovementRepository movementRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository
    ) {
        this.movementRepository = movementRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public ExpensesByCategoryResponse execute(String authenticatedEmail, Integer month, Integer year) {
        User user = userRepository.findByEmail(authenticatedEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Movement> expenses = movementRepository.findByUserIdAndDateBetween(
                user.id(),
                startDate,
                endDate
        ).stream()
                .filter(movement -> movement.type() == MovementType.EXPENSE)
                .toList();

        // Calcular total de gastos
        BigDecimal totalExpenses = expenses.stream()
                .map(movement -> movement.amount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Si no hay gastos, retornar respuesta vacía
        if (totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return new ExpensesByCategoryResponse(
                    year + "-" + String.format("%02d", month),
                    BigDecimal.ZERO,
                    new ArrayList<>()
            );
        }

        // Agrupar gastos por categoría
        Map<Long, BigDecimal> expensesByCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        Movement::categoryId,
                        Collectors.mapping(
                                movement -> movement.amount().abs(),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        // Construir la lista de respuesta con información de categorías
        BigDecimal finalTotalExpenses = totalExpenses;
        List<CategoryExpense> categories = expensesByCategory.entrySet().stream()
                .map(entry -> {
                    Long categoryId = entry.getKey();
                    BigDecimal amount = entry.getValue();
                    BigDecimal percentage = calculatePercentage(amount, finalTotalExpenses);

                    // Obtener información de la categoría
                    String categoryName = "Sin categoría";
                    String color = "#D3D3D3";

                    if (categoryId != null) {
                        Category category = categoryRepository.findById(categoryId).orElse(null);
                        if (category != null) {
                            categoryName = category.name();
                            color = category.color();
                        }
                    }

                    return new CategoryExpense(
                            categoryId,
                            categoryName,
                            color,
                            amount,
                            percentage
                    );
                })
                .sorted((a, b) -> b.amount().compareTo(a.amount()))
                .toList();

        String period = year + "-" + String.format("%02d", month);

        return new ExpensesByCategoryResponse(
                period,
                totalExpenses,
                categories
        );
    }

    private BigDecimal calculatePercentage(BigDecimal amount, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return amount.divide(total, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
