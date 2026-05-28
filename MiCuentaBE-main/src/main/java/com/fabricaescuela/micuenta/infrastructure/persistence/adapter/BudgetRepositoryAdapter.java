package com.fabricaescuela.micuenta.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fabricaescuela.micuenta.domain.model.Budget;
import com.fabricaescuela.micuenta.domain.repository.BudgetRepository;
import com.fabricaescuela.micuenta.infrastructure.persistence.entity.BudgetEntity;
import com.fabricaescuela.micuenta.infrastructure.persistence.mapper.BudgetMapper;
import com.fabricaescuela.micuenta.infrastructure.persistence.repository.BudgetJpaRepository;

@Repository
public class BudgetRepositoryAdapter implements BudgetRepository {

    private final BudgetJpaRepository budgetJpaRepository;
    private final BudgetMapper budgetMapper;

    public BudgetRepositoryAdapter(BudgetJpaRepository budgetJpaRepository, BudgetMapper budgetMapper) {
        this.budgetJpaRepository = budgetJpaRepository;
        this.budgetMapper = budgetMapper;
    }

    @Override
    public Budget save(Budget budget) {
        BudgetEntity saved = budgetJpaRepository.save(budgetMapper.toEntity(budget));
        return budgetMapper.toDomain(saved);
    }

    @Override
    public Optional<Budget> findByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year) {
        return budgetJpaRepository.findByUserIdAndCategoryIdAndMonthAndYear(userId, categoryId, month, year)
                .map(budgetMapper::toDomain);
    }

    @Override
    public List<Budget> findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year) {
        return budgetJpaRepository.findByUserIdAndMonthAndYear(userId, month, year)
                .stream()
                .map(budgetMapper::toDomain)
                .toList();
    }

    @Override
    public List<Budget> findByUserId(Long userId) {
        return budgetJpaRepository.findByUserId(userId)
                .stream()
                .map(budgetMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Budget> findById(Long id) {
        return budgetJpaRepository.findById(id)
                .map(budgetMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        budgetJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year) {
        return budgetJpaRepository.existsByUserIdAndCategoryIdAndMonthAndYear(userId, categoryId, month, year);
    }
}
