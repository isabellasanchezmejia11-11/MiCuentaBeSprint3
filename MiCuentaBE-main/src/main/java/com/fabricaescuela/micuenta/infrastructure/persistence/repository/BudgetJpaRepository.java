package com.fabricaescuela.micuenta.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fabricaescuela.micuenta.infrastructure.persistence.entity.BudgetEntity;

public interface BudgetJpaRepository extends JpaRepository<BudgetEntity, Long> {
    Optional<BudgetEntity> findByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year);
    
    List<BudgetEntity> findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year);
    
    List<BudgetEntity> findByUserId(Long userId);
    
    boolean existsByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year);
}
