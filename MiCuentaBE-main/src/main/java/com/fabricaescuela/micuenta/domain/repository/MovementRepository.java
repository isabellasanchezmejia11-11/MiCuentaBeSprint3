package com.fabricaescuela.micuenta.domain.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.fabricaescuela.micuenta.domain.model.Movement;
import com.fabricaescuela.micuenta.domain.model.MovementType;

public interface MovementRepository {
    Movement save(Movement movement);

    Optional<Movement> findById(Long id);

    Movement update(Movement movement);

    void deleteById(Long id);

    List<Movement> findByUserIdAndType(Long userId, MovementType type);

    List<Movement> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Movement> findByUserIdWithFilters(Long userId, MovementType type, Long categoryId, LocalDate startDate, LocalDate endDate);

    BigDecimal sumAmountByUserIdAndType(Long userId, MovementType type);

    /**
     * Suma los montos de movimientos de un usuario, por categoría, en un rango de fechas y tipo.
     * Útil para calcular el valor ejecutado de un presupuesto.
     */
    BigDecimal sumAmountByUserIdAndCategoryIdAndDateBetweenAndType(Long userId, Long categoryId, LocalDate startDate, LocalDate endDate, MovementType type);
}