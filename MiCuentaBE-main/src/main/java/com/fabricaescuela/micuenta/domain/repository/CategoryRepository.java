package com.fabricaescuela.micuenta.domain.repository;
import com.fabricaescuela.micuenta.domain.model.Category;
import com.fabricaescuela.micuenta.domain.model.MovementType;

import java.util.List;
import java.util.Optional;



public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(Long id);

    List<Category> findByUserIdOrUserIdIsNullAndType(Long userId, MovementType type);

    boolean existsByNameAndUserId(String name, Long userId);

    boolean existsByNameAndUserIdAndIdNot(String name, Long userId, Long id);

    boolean hasMovements(Long categoryId);

    void deleteById(Long id); 
}