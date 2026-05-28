package com.fabricaescuela.micuenta.infrastructure.persistence.adapter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fabricaescuela.micuenta.domain.model.Movement;
import com.fabricaescuela.micuenta.domain.model.MovementType;
import com.fabricaescuela.micuenta.domain.repository.MovementRepository;
import com.fabricaescuela.micuenta.infrastructure.persistence.entity.MovementEntity;
import com.fabricaescuela.micuenta.infrastructure.persistence.mapper.MovementMapper;
import com.fabricaescuela.micuenta.infrastructure.persistence.repository.MovementJpaRepository;

@Repository
public class MovementRepositoryAdapter implements MovementRepository {

    private final MovementJpaRepository movementJpaRepository;
    private final MovementMapper movementMapper;

    public MovementRepositoryAdapter(
            MovementJpaRepository movementJpaRepository,
            MovementMapper movementMapper
    ) {
        this.movementJpaRepository = movementJpaRepository;
        this.movementMapper = movementMapper;
    }

    @Override
    public Movement save(Movement movement) {
        MovementEntity saved = movementJpaRepository.save(movementMapper.toEntity(movement));
        return movementMapper.toDomain(saved);
    }

    @Override
    public Optional<Movement> findById(Long id) {
        return movementJpaRepository.findById(id)
                .map(movementMapper::toDomain);
    }

    @Override
    public Movement update(Movement movement) {
        MovementEntity updated = movementJpaRepository.save(movementMapper.toEntity(movement));
        return movementMapper.toDomain(updated);
    }

    @Override
    public void deleteById(Long id) {
        movementJpaRepository.deleteById(id);
    }

    @Override
    public List<Movement> findByUserIdAndType(Long userId, MovementType type) {
        return movementJpaRepository.findByUserIdAndTypeOrderByDateDescIdDesc(userId, type)
                .stream()
                .map(movementMapper::toDomain)
                .toList();
    }

    @Override
    public List<Movement> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate) {
        return movementJpaRepository.findByUserIdAndDateBetweenOrderByDateDescIdDesc(userId, startDate, endDate)
                .stream()
                .map(movementMapper::toDomain)
                .toList();
    }

    @Override
    public List<Movement> findByUserIdWithFilters(Long userId, MovementType type, Long categoryId, LocalDate startDate, LocalDate endDate) {
        return movementJpaRepository.findByUserIdWithFilters(userId, type, categoryId, startDate, endDate)
                .stream()
                .map(movementMapper::toDomain)
                .toList();
    }

    @Override
    public BigDecimal sumAmountByUserIdAndType(Long userId, MovementType type) {
        return movementJpaRepository.sumAmountByUserIdAndType(userId, type);
    }

    @Override
    public BigDecimal sumAmountByUserIdAndCategoryIdAndDateBetweenAndType(Long userId, Long categoryId, LocalDate startDate, LocalDate endDate, MovementType type) {
        return movementJpaRepository.sumAmountByUserIdAndCategoryIdAndDateBetweenAndType(userId, categoryId, startDate, endDate, type);
    }
}