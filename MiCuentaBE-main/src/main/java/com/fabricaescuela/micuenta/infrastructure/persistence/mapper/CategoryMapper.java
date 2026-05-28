package com.fabricaescuela.micuenta.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.fabricaescuela.micuenta.domain.model.Category;
import com.fabricaescuela.micuenta.infrastructure.persistence.entity.CategoryEntity;

@Component
public class CategoryMapper {

    public Category toDomain(CategoryEntity e) {
        return new Category(e.getId(), e.getName(), e.getType(), e.getUserId(), e.getColor());
    }

    public CategoryEntity toEntity(Category c) {
        return new CategoryEntity(c.id(), c.name(), c.type(), c.userId(), c.color());
    }
}