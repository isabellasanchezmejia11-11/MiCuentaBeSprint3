package com.fabricaescuela.micuenta.application.usecase;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.fabricaescuela.micuenta.application.dto.request.CreateMovementRequest;
import com.fabricaescuela.micuenta.application.dto.response.MovementResponse;
import com.fabricaescuela.micuenta.application.exception.ResourceNotFoundException;
import com.fabricaescuela.micuenta.domain.model.Category;
import com.fabricaescuela.micuenta.domain.model.Movement;
import com.fabricaescuela.micuenta.domain.model.MovementType;
import com.fabricaescuela.micuenta.domain.model.User;
import com.fabricaescuela.micuenta.domain.repository.CategoryRepository;
import com.fabricaescuela.micuenta.domain.repository.MovementRepository;
import com.fabricaescuela.micuenta.domain.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class RegisterIncomeUseCase {

    private final MovementRepository movementRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public RegisterIncomeUseCase(
            MovementRepository movementRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository
    ) {
        this.movementRepository = movementRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public MovementResponse execute(String authenticatedEmail, CreateMovementRequest request) {

        User user = userRepository.findByEmail(authenticatedEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        LocalDateTime now = LocalDateTime.now();
        Movement savedMovement = movementRepository.save(
                new Movement(
                        null,
                        user.id(),
                        request.amount(),
                        request.date(),
                        MovementType.INCOME,
                        request.categoryId(),
                        normalizeDescription(request.description()),
                        now,
                        now
                )
        );

        return toResponse(savedMovement);
    }

    private String normalizeDescription(String description) {
        return (description == null || description.trim().isEmpty())
                ? null
                : description.trim();
    }

    private MovementResponse toResponse(Movement movement) {
        return new MovementResponse(
                movement.id(),
                movement.categoryId(),
                movement.amount(),
                movement.date(),
                movement.type(),
                categoryRepository.findById(movement.categoryId())
                        .map(Category::name)
                        .orElse("Sin categoría"),
                movement.description()
        );
    }
}