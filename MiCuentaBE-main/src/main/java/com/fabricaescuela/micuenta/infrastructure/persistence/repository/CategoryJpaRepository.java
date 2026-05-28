package com.fabricaescuela.micuenta.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fabricaescuela.micuenta.infrastructure.persistence.entity.CategoryEntity;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {

    @Query(value = "SELECT * FROM categories WHERE (user_id = :userId OR user_id IS NULL) AND type = :type", nativeQuery = true)
    List<CategoryEntity> findByUserIdOrUserIdIsNullAndType(@Param("userId") Long userId, @Param("type") String type);

    boolean existsByNameAndUserId(String name, Long userId);

    boolean existsByNameAndUserIdAndIdNot(String name, Long userId, Long id);
}