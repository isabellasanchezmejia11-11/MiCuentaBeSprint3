package com.fabricaescuela.micuenta.application.usecase;

import org.springframework.stereotype.Service;

import com.fabricaescuela.micuenta.application.dto.request.CategoryRequest;
import com.fabricaescuela.micuenta.application.dto.response.CategoryResponse;
import com.fabricaescuela.micuenta.domain.model.Category;
import com.fabricaescuela.micuenta.domain.repository.CategoryRepository;

@Service
public class UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public UpdateCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse execute(Long id, CategoryRequest request) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (categoryRepository.existsByNameAndUserIdAndIdNot(request.name(), existing.userId(), id)) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        }

        Category updated = new Category(
                existing.id(),
                request.name(),
                request.type(),
                existing.userId(),
                request.color()
        );

        Category saved = categoryRepository.save(updated);

        return new CategoryResponse(
                saved.id(),
                saved.name(),
                saved.type(),
                saved.userId() != null,
                saved.userId(),
                saved.color()
        );
    }
}
