package com.fabricaescuela.micuenta.application.usecase;


import org.springframework.stereotype.Service;

import com.fabricaescuela.micuenta.domain.repository.CategoryRepository;

@Service
public class DeleteCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public DeleteCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void execute(Long categoryId) {

        
        if (categoryRepository.hasMovements(categoryId)) {
            throw new IllegalArgumentException(
                "No puedes eliminar una categoría con movimientos asociados"
            );
        }

        categoryRepository.deleteById(categoryId);
    }
}