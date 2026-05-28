package com.fabricaescuela.micuenta.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fabricaescuela.micuenta.application.dto.response.DashboardSummaryResponse;
import com.fabricaescuela.micuenta.application.dto.response.MovementResponse;
import com.fabricaescuela.micuenta.application.exception.ResourceNotFoundException;
import com.fabricaescuela.micuenta.domain.model.Category;
import com.fabricaescuela.micuenta.domain.model.Movement;
import com.fabricaescuela.micuenta.domain.model.MovementType;
import com.fabricaescuela.micuenta.domain.model.User;
import com.fabricaescuela.micuenta.domain.repository.MovementRepository;
import com.fabricaescuela.micuenta.domain.repository.UserRepository;
import com.fabricaescuela.micuenta.infrastructure.persistence.repository.MovementJpaRepository;
import com.fabricaescuela.micuenta.domain.repository.CategoryRepository;


@Service
public class GetMonthlyDashboardSummaryUseCase {

    private final MovementRepository movementRepository;
    private final UserRepository userRepository;
    private final MovementJpaRepository movementJpaRepository;
    private final CategoryRepository categoryRepository;

    public GetMonthlyDashboardSummaryUseCase(
            MovementRepository movementRepository,
            UserRepository userRepository,
            MovementJpaRepository movementJpaRepository,
            CategoryRepository categoryRepository
    ) {
        this.movementRepository = movementRepository;
        this.userRepository = userRepository;
        this.movementJpaRepository = movementJpaRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse execute(String authenticatedEmail) {
        User user = userRepository.findByEmail(authenticatedEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.withDayOfMonth(1);
        LocalDate endDate = today.withDayOfMonth(today.lengthOfMonth());

        List<Movement> monthlyMovements = movementRepository.findByUserIdAndDateBetween(
                user.id(),
                startDate,
                endDate
        );

        BigDecimal monthlyIncome = sumByType(monthlyMovements, MovementType.INCOME);
        BigDecimal monthlyExpense = sumByType(monthlyMovements, MovementType.EXPENSE);
        BigDecimal monthlyNet = monthlyIncome.subtract(monthlyExpense);

        BigDecimal totalIncome = safe(
                movementRepository.sumAmountByUserIdAndType(user.id(), MovementType.INCOME)
        );
        BigDecimal totalExpense = safe(
                movementRepository.sumAmountByUserIdAndType(user.id(), MovementType.EXPENSE)
        );
        BigDecimal currentBalance = totalIncome.subtract(totalExpense);

        List<MovementResponse> movementResponses = monthlyMovements.stream()
                .map(this::toResponse)
                .toList();

        String month = today.getYear() + "-" + String.format("%02d", today.getMonthValue());

        return new DashboardSummaryResponse(
                month,
                monthlyIncome,
                monthlyExpense,
                monthlyNet,
                currentBalance,
                movementResponses
        );
    }

    private BigDecimal sumByType(List<Movement> movements, MovementType type) {
        return movements.stream()
                .filter(movement -> movement.type() == type)
                .map(movement -> type == MovementType.EXPENSE ? movement.amount().abs() : movement.amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private MovementResponse toResponse(Movement movement) {
        Long categoryId = movement.categoryId();
        String categoryName = "Sin categoría";
        
        if (categoryId != null) {
            categoryName = categoryRepository.findById(categoryId)
                    .map(Category::name)
                    .orElse("Sin categoría");
        }
        
        return new MovementResponse(
                movement.id(),
                categoryId,
                movement.amount(),
                movement.date(),
                movement.type(),
                categoryName,
                movement.description()
        );
    }
}