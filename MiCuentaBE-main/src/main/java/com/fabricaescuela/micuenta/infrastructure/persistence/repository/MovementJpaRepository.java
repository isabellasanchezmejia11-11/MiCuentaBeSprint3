package com.fabricaescuela.micuenta.infrastructure.persistence.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fabricaescuela.micuenta.domain.model.MovementType;
import com.fabricaescuela.micuenta.infrastructure.persistence.entity.MovementEntity;

public interface MovementJpaRepository extends JpaRepository<MovementEntity, Long> {

    List<MovementEntity> findByUserIdAndTypeOrderByDateDescIdDesc(Long userId, MovementType type);

    List<MovementEntity> findByUserIdAndDateBetweenOrderByDateDescIdDesc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
            SELECT m FROM MovementEntity m
            WHERE m.userId = :userId
              AND (:type IS NULL OR m.type = :type)
              AND (:categoryId IS NULL OR m.categoryId = :categoryId)
              AND (:startDate IS NULL OR m.date >= :startDate)
              AND (:endDate IS NULL OR m.date <= :endDate)
            ORDER BY m.date DESC, m.id DESC
            """)
    List<MovementEntity> findByUserIdWithFilters(
            @Param("userId") Long userId,
            @Param("type") MovementType type,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT COALESCE(SUM(m.amount), 0)
            FROM MovementEntity m
            WHERE m.userId = :userId
              AND m.type = :type
            """)
    BigDecimal sumAmountByUserIdAndType(
            @Param("userId") Long userId,
            @Param("type") MovementType type
    );

    // 🔥 ESTE ES EL QUE TE FALTABA
    boolean existsByCategoryId(Long categoryId);

    @Query("""
            SELECT COALESCE(SUM(m.amount), 0)
            FROM MovementEntity m
            WHERE m.userId = :userId
              AND m.categoryId = :categoryId
              AND m.date >= :startDate
              AND m.date <= :endDate
              AND m.type = :type
            """)
    BigDecimal sumAmountByUserIdAndCategoryIdAndDateBetweenAndType(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") MovementType type
    );
}