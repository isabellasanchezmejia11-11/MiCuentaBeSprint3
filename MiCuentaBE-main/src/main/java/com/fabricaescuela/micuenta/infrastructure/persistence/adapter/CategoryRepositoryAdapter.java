package com.fabricaescuela.micuenta.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fabricaescuela.micuenta.domain.model.Category;
import com.fabricaescuela.micuenta.domain.model.MovementType;
import com.fabricaescuela.micuenta.domain.repository.CategoryRepository;
import com.fabricaescuela.micuenta.infrastructure.persistence.mapper.CategoryMapper;
import com.fabricaescuela.micuenta.infrastructure.persistence.repository.CategoryJpaRepository;
import com.fabricaescuela.micuenta.infrastructure.persistence.repository.MovementJpaRepository;

@Repository
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpa;
    private final CategoryMapper mapper;
    private final MovementJpaRepository movementJpaRepository; // 🔥 nuevo

    public CategoryRepositoryAdapter(
            CategoryJpaRepository jpa,
            CategoryMapper mapper,
            MovementJpaRepository movementJpaRepository // 🔥 nuevo
    ) {
        this.jpa = jpa;
        this.mapper = mapper;
        this.movementJpaRepository = movementJpaRepository;
    }

    @Override
    public Category save(Category category) {
        return mapper.toDomain(jpa.save(mapper.toEntity(category)));
    }

    @Override
    public List<Category> findByUserIdOrUserIdIsNullAndType(Long userId, MovementType type) {
        return jpa.findByUserIdOrUserIdIsNullAndType(userId, type.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByNameAndUserId(String name, Long userId) {
        return jpa.existsByNameAndUserId(name, userId);
    }

    @Override
    public boolean existsByNameAndUserIdAndIdNot(String name, Long userId, Long id) {
        return jpa.existsByNameAndUserIdAndIdNot(name, userId, id);
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    // 🔥 CORREGIDO
    @Override
    public Optional<Category> findById(Long id) {
        return jpa.findById(id)
                .map(mapper::toDomain);
    }

    // 🔥 IMPLEMENTACIÓN REAL
    @Override
    public boolean hasMovements(Long categoryId) {
        return movementJpaRepository.existsByCategoryId(categoryId);
    }
}