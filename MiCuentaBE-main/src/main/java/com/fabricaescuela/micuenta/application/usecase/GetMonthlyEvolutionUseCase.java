package com.fabricaescuela.micuenta.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fabricaescuela.micuenta.application.dto.response.EvolutionResponse;
import com.fabricaescuela.micuenta.application.dto.response.MonthlyDataResponse;
import com.fabricaescuela.micuenta.application.exception.ResourceNotFoundException;
import com.fabricaescuela.micuenta.domain.model.Movement;
import com.fabricaescuela.micuenta.domain.model.MovementType;
import com.fabricaescuela.micuenta.domain.model.User;
import com.fabricaescuela.micuenta.domain.repository.MovementRepository;
import com.fabricaescuela.micuenta.domain.repository.UserRepository;

@Service
public class GetMonthlyEvolutionUseCase {

    private final MovementRepository movementRepository;
    private final UserRepository userRepository;
    private static final int MONTHS_TO_SHOW = 6;

    public GetMonthlyEvolutionUseCase(
            MovementRepository movementRepository,
            UserRepository userRepository
    ) {
        this.movementRepository = movementRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public EvolutionResponse execute(String authenticatedEmail) {
        User user = userRepository.findByEmail(authenticatedEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusMonths(MONTHS_TO_SHOW - 1).withDayOfMonth(1);
        LocalDate endDate = today.withDayOfMonth(today.lengthOfMonth());

        List<Movement> movements = movementRepository.findByUserIdAndDateBetween(
                user.id(),
                startDate,
                endDate
        );

        // Agrupar por año-mes
        Map<YearMonth, List<Movement>> movementsByMonth = movements.stream()
                .collect(Collectors.groupingBy(
                        m -> YearMonth.from(m.date()),
                        Collectors.toList()
                ));

        // Generar datos para los últimos 6 meses
        List<MonthlyDataResponse> monthlyData = new ArrayList<>();
        for (int i = MONTHS_TO_SHOW - 1; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            List<Movement> monthMovements = movementsByMonth.getOrDefault(month, new ArrayList<>());

            BigDecimal income = calculateTotal(monthMovements, MovementType.INCOME);
            BigDecimal expense = calculateTotal(monthMovements, MovementType.EXPENSE);
            BigDecimal net = income.subtract(expense);

            monthlyData.add(new MonthlyDataResponse(
                    month.toString(),
                    income,
                    expense,
                    net
            ));
        }

        return new EvolutionResponse(monthlyData);
    }

    private BigDecimal calculateTotal(List<Movement> movements, MovementType type) {
        return movements.stream()
                .filter(m -> m.type() == type)
                .map(Movement::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
