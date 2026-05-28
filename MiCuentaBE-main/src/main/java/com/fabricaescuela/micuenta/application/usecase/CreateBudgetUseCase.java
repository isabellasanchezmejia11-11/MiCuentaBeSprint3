package com.fabricaescuela.micuenta.application.usecase;

import org.springframework.stereotype.Service;

import com.fabricaescuela.micuenta.application.dto.request.CreateBudgetRequest;
import com.fabricaescuela.micuenta.application.dto.response.BudgetResponse;
import com.fabricaescuela.micuenta.application.exception.BudgetAlreadyExistsException;
import com.fabricaescuela.micuenta.application.exception.ResourceNotFoundException;
import com.fabricaescuela.micuenta.domain.model.Budget;
import com.fabricaescuela.micuenta.domain.model.Category;
import com.fabricaescuela.micuenta.domain.model.User;
import com.fabricaescuela.micuenta.domain.repository.BudgetRepository;
import com.fabricaescuela.micuenta.domain.repository.CategoryRepository;
import com.fabricaescuela.micuenta.domain.repository.UserRepository;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;

@Service
public class CreateBudgetUseCase {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public CreateBudgetUseCase(
            BudgetRepository budgetRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public BudgetResponse execute(String authenticatedEmail, CreateBudgetRequest request) {
        // Validar que el monto sea mayor a cero
        if (request.amountLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }

        // Validar que el mes esté en rango válido
        if (request.month() < 1 || request.month() > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12");
        }

        // Obtener el usuario autenticado
        User user = userRepository.findByEmail(authenticatedEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        // Validar que la categoría existe
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Validar que no existe un presupuesto duplicado para esta categoría en el mes/año seleccionado
        boolean budgetExists = budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                user.id(),
                request.categoryId(),
                request.month(),
                request.year()
        );

        if (budgetExists) {
            throw new BudgetAlreadyExistsException(
                    "Ya existe un presupuesto activo para esta categoría en el mes seleccionado"
            );
        }

        // Crear y guardar el presupuesto
        Budget budget = new Budget(
                null,
                request.amountLimit(),
                request.alertPercent(),
                request.month(),
                request.year(),
                request.categoryId(),
                user.id()
        );

        Budget savedBudget = budgetRepository.save(budget);

        // Devolver el response
        return toResponse(savedBudget, category);
    }

    private BudgetResponse toResponse(Budget budget, Category category) {
        return new BudgetResponse(
                budget.getId(),
                budget.getAmountLimit(),
                budget.getAlertPercent(),
                budget.getMonth(),
                budget.getYear(),
                budget.getCategoryId(),
                category.name(),
                category.type().name(),
                budget.getUserId(),
                null
        );
    }
}