package com.fabricaescuela.micuenta.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.fabricaescuela.micuenta.domain.model.Budget;
import com.fabricaescuela.micuenta.infrastructure.persistence.entity.BudgetEntity;

@Component
public class BudgetMapper {

    public Budget toDomain(BudgetEntity entity) {
        return new Budget(
                entity.getId(),
                entity.getAmountLimit(),
                entity.getAlertPercent(),
                entity.getMonth(),
                entity.getYear(),
                entity.getCategoryId(),
                entity.getUserId()
        );
    }

    public BudgetEntity toEntity(Budget budget) {
        return new BudgetEntity(
                budget.getId(),
                budget.getAmountLimit(),
                budget.getAlertPercent(),
                budget.getMonth(),
                budget.getYear(),
                budget.getCategoryId(),
                budget.getUserId()
        );
    }
}
