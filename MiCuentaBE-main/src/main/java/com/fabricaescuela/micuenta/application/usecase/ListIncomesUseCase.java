package com.fabricaescuela.micuenta.application.usecase;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fabricaescuela.micuenta.application.dto.response.MovementResponse;
import com.fabricaescuela.micuenta.application.exception.ResourceNotFoundException;
import com.fabricaescuela.micuenta.domain.model.Category;
import com.fabricaescuela.micuenta.domain.model.Movement;
import com.fabricaescuela.micuenta.domain.model.MovementType;
import com.fabricaescuela.micuenta.domain.model.User;
import com.fabricaescuela.micuenta.domain.repository.CategoryRepository;
import com.fabricaescuela.micuenta.domain.repository.MovementRepository;
import com.fabricaescuela.micuenta.domain.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ListIncomesUseCase {

    private final MovementRepository movementRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ListIncomesUseCase(
            MovementRepository movementRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository
    ) {
        this.movementRepository = movementRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<MovementResponse> execute(String authenticatedEmail, Optional<LocalDate> startDate, Optional<LocalDate> endDate) {

        User user = userRepository.findByEmail(authenticatedEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        List<Movement> movements = movementRepository.findByUserIdAndType(user.id(), MovementType.INCOME);

        return movements.stream()
                .filter(movement -> startDate.map(date -> !movement.date().isBefore(date)).orElse(true))
                .filter(movement -> endDate.map(date -> !movement.date().isAfter(date)).orElse(true))
                .map(this::toResponse)
                .toList();
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