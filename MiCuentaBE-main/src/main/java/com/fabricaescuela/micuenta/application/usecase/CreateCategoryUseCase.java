package com.fabricaescuela.micuenta.application.usecase;

import org.springframework.stereotype.Service;

import com.fabricaescuela.micuenta.application.dto.response.CategoryResponse;
import com.fabricaescuela.micuenta.domain.model.Category;
import com.fabricaescuela.micuenta.domain.model.MovementType;
import com.fabricaescuela.micuenta.domain.model.User;
import com.fabricaescuela.micuenta.domain.repository.CategoryRepository;
import com.fabricaescuela.micuenta.domain.repository.UserRepository;

@Service
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CreateCategoryUseCase(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public CategoryResponse execute(String email, String name, MovementType type, String color) {

        User user = userRepository.findByEmail(email).orElseThrow();

        if (categoryRepository.existsByNameAndUserId(name, user.id())) {
            throw new IllegalArgumentException("Ya existe esa categoría");
        }

        Category saved = categoryRepository.save(new Category(null, name, type, user.id(), color));

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