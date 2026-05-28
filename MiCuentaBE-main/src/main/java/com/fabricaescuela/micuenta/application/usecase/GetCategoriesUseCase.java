package com.fabricaescuela.micuenta.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fabricaescuela.micuenta.domain.model.Category;
import com.fabricaescuela.micuenta.domain.model.MovementType;
import com.fabricaescuela.micuenta.domain.model.User;
import com.fabricaescuela.micuenta.domain.repository.CategoryRepository;
import com.fabricaescuela.micuenta.domain.repository.UserRepository;

@Service
public class GetCategoriesUseCase {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public GetCategoriesUseCase(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<Category> execute(String email, MovementType type) {

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        return categoryRepository.findByUserIdOrUserIdIsNullAndType(user.id(), type);
    }
}