package com.fabricaescuela.micuenta.application.usecase;

import org.springframework.stereotype.Service;

import com.fabricaescuela.micuenta.application.dto.request.CreateBudgetRequest;
import com.fabricaescuela.micuenta.application.dto.response.BudgetResponse;
import com.fabricaescuela.micuenta.application.exception.ResourceNotFoundException;
import com.fabricaescuela.micuenta.domain.model.Budget;
import com.fabricaescuela.micuenta.domain.model.Category;
import com.fabricaescuela.micuenta.domain.repository.BudgetRepository;
import com.fabricaescuela.micuenta.domain.repository.CategoryRepository;

@Service
public class UpdateBudgetUseCase {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    public UpdateBudgetUseCase(BudgetRepository budgetRepository, CategoryRepository categoryRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
    }

    public BudgetResponse execute(Long id, CreateBudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        if (request.amountLimit() != null) {
            budget.setAmountLimit(request.amountLimit());
        }
        if (request.month() != null) {
            budget.setMonth(request.month());
        }
        if (request.year() != null) {
            budget.setYear(request.year());
        }
        if (request.categoryId() != null) {
            budget.setCategoryId(request.categoryId());
        }
        if (request.alertPercent() != null) {
            budget.setAlertPercent(request.alertPercent());
        }

        Budget savedBudget = budgetRepository.save(budget);

        // Load category for response
        Category category = categoryRepository.findById(savedBudget.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + savedBudget.getCategoryId()));

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
