package com.fabricaescuela.micuenta.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fabricaescuela.micuenta.application.dto.response.FinancialAdviceAiContent;
import com.fabricaescuela.micuenta.application.dto.response.FinancialAdviceResponse;
import com.fabricaescuela.micuenta.application.exception.ResourceNotFoundException;
import com.fabricaescuela.micuenta.domain.model.Budget;
import com.fabricaescuela.micuenta.domain.model.Category;
import com.fabricaescuela.micuenta.domain.model.Movement;
import com.fabricaescuela.micuenta.domain.model.MovementType;
import com.fabricaescuela.micuenta.domain.model.User;
import com.fabricaescuela.micuenta.domain.repository.BudgetRepository;
import com.fabricaescuela.micuenta.domain.repository.CategoryRepository;
import com.fabricaescuela.micuenta.domain.repository.MovementRepository;
import com.fabricaescuela.micuenta.domain.repository.UserRepository;
import com.fabricaescuela.micuenta.infrastructure.ai.gemini.GeminiFinancialAdviceClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GenerateFinancialAdviceUseCase {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final MovementRepository movementRepository;
    private final BudgetRepository budgetRepository;
    private final GeminiFinancialAdviceClient geminiFinancialAdviceClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GenerateFinancialAdviceUseCase(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            MovementRepository movementRepository,
            BudgetRepository budgetRepository,
            GeminiFinancialAdviceClient geminiFinancialAdviceClient
    ) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.movementRepository = movementRepository;
        this.budgetRepository = budgetRepository;
        this.geminiFinancialAdviceClient = geminiFinancialAdviceClient;
    }

    @Transactional(readOnly = true)
    public FinancialAdviceResponse execute(String authenticatedEmail, Integer month, Integer year) {
        User user = userRepository.findByEmail(authenticatedEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        String period = year + "-" + String.format("%02d", month);

        List<Category> categories = Stream.concat(
                        categoryRepository.findByUserIdOrUserIdIsNullAndType(user.id(), MovementType.INCOME).stream(),
                        categoryRepository.findByUserIdOrUserIdIsNullAndType(user.id(), MovementType.EXPENSE).stream()
                )
                .filter(category -> category.id() != null)
                .collect(Collectors.toMap(Category::id, Function.identity(), (a, b) -> a, LinkedHashMap::new))
                .values()
                .stream()
                .toList();

        Map<Long, Category> categoriesById = categories.stream()
                .filter(category -> category.id() != null)
                .collect(Collectors.toMap(Category::id, Function.identity(), (a, b) -> a));

        List<Movement> movements = movementRepository.findByUserIdAndDateBetween(user.id(), startDate, endDate);
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(user.id(), month, year);

        BigDecimal monthlyIncome = sumByType(movements, MovementType.INCOME);
        BigDecimal monthlyExpense = sumByType(movements, MovementType.EXPENSE);
        BigDecimal monthlyNet = monthlyIncome.subtract(monthlyExpense);
        BigDecimal savingsRate = percentage(monthlyNet, monthlyIncome);

        String contextJson = toJson(buildFinancialContext(
                user,
                period,
                startDate,
                endDate,
                categories,
                categoriesById,
                movements,
                budgets,
                monthlyIncome,
                monthlyExpense,
                monthlyNet,
                savingsRate
        ));

        FinancialAdviceAiContent advice = geminiFinancialAdviceClient.generateAdvice(contextJson);

        return new FinancialAdviceResponse(
                period,
                advice.headline(),
                advice.summary(),
                advice.healthStatus(),
                monthlyIncome,
                monthlyExpense,
                monthlyNet,
                savingsRate,
                safeList(advice.insights()),
                safeList(advice.actionItems()),
                safeList(advice.categoryRecommendations()),
                safeList(advice.alerts()),
                advice.disclaimer(),
                LocalDateTime.now()
        );
    }

    private Map<String, Object> buildFinancialContext(
            User user,
            String period,
            LocalDate startDate,
            LocalDate endDate,
            List<Category> categories,
            Map<Long, Category> categoriesById,
            List<Movement> movements,
            List<Budget> budgets,
            BigDecimal monthlyIncome,
            BigDecimal monthlyExpense,
            BigDecimal monthlyNet,
            BigDecimal savingsRate
    ) {
        Map<Long, BigDecimal> expenseByCategory = movements.stream()
                .filter(movement -> movement.type() == MovementType.EXPENSE)
                .collect(Collectors.groupingBy(
                        Movement::categoryId,
                        LinkedHashMap::new,
                        Collectors.mapping(movement -> movement.amount().abs(), Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        Map<Long, Budget> budgetByCategory = budgets.stream()
                .filter(budget -> budget.getCategoryId() != null)
                .collect(Collectors.toMap(Budget::getCategoryId, Function.identity(), (a, b) -> a));

        List<Map<String, Object>> categoriesPayload = expenseByCategory.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .map(entry -> {
                    Long categoryId = entry.getKey();
                    BigDecimal amount = entry.getValue();
                    Category category = categoriesById.get(categoryId);
                    Budget budget = budgetByCategory.get(categoryId);

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("categoryId", categoryId);
                    item.put("categoryName", category != null ? category.name() : "Sin categoría");
                    item.put("color", category != null ? category.color() : "#D3D3D3");
                    item.put("amount", amount);
                    item.put("percentage", percentage(amount, monthlyExpense));
                    item.put("budgetLimit", budget != null ? budget.getAmountLimit() : null);
                    item.put("budgetUsagePercent", budget != null ? percentage(amount, budget.getAmountLimit()) : null);
                    item.put("budgetAlertPercent", budget != null ? budget.getAlertPercent() : null);
                    return item;
                })
                .toList();

        List<Map<String, Object>> budgetsPayload = budgets.stream()
                .map(budget -> {
                    Category category = categoriesById.get(budget.getCategoryId());
                    BigDecimal executed = expenseByCategory.getOrDefault(budget.getCategoryId(), BigDecimal.ZERO);

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("categoryId", budget.getCategoryId());
                    item.put("categoryName", category != null ? category.name() : "Sin categoría");
                    item.put("limit", budget.getAmountLimit());
                    item.put("executed", executed);
                    item.put("available", budget.getAmountLimit().subtract(executed));
                    item.put("usagePercent", percentage(executed, budget.getAmountLimit()));
                    item.put("alertPercent", budget.getAlertPercent());
                    return item;
                })
                .toList();

        List<Map<String, Object>> movementsPayload = movements.stream()
                .sorted(Comparator.comparing(Movement::date).reversed().thenComparing(Movement::id, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(30)
                .map(movement -> {
                    Category category = categoriesById.get(movement.categoryId());

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("date", movement.date() != null ? movement.date().toString() : null);
                    item.put("type", movement.type().name());
                    item.put("amount", movement.amount().abs());
                    item.put("categoryName", category != null ? category.name() : "Sin categoría");
                    item.put("description", sanitize(movement.description()));
                    return item;
                })
                .toList();

        List<Map<String, Object>> allCategoriesPayload = categories.stream()
                .map(category -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", category.id());
                    item.put("name", category.name());
                    item.put("type", category.type() != null ? category.type().name() : null);
                    item.put("color", category.color());
                    return item;
                })
                .toList();

        Map<String, Object> userPayload = new LinkedHashMap<>();
        userPayload.put("name", user.name());
        userPayload.put("lastname", user.lastname());

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("user", userPayload);
        context.put("period", Map.of(
                "value", period,
                "startDate", startDate.toString(),
                "endDate", endDate.toString()
        ));
        context.put("summary", Map.of(
                "monthlyIncome", monthlyIncome,
                "monthlyExpense", monthlyExpense,
                "monthlyNet", monthlyNet,
                "savingsRate", savingsRate,
                "movementCount", movements.size(),
                "budgetCount", budgets.size()
        ));
        context.put("expenseCategories", categoriesPayload);
        context.put("budgets", budgetsPayload);
        context.put("movements", movementsPayload);
        context.put("availableCategories", allCategoriesPayload);
        return context;
    }

    private BigDecimal sumByType(List<Movement> movements, MovementType type) {
        return movements.stream()
                .filter(movement -> movement.type() == type)
                .map(movement -> movement.amount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal percentage(BigDecimal amount, BigDecimal total) {
        if (amount == null || total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return amount
                .multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No fue posible preparar el contexto financiero para Gemini.", ex);
        }
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        String clean = value.replaceAll("[\\r\\n\\t]+", " ").trim();
        return clean.length() > 120 ? clean.substring(0, 120) + "..." : clean;
    }

    private <T> List<T> safeList(List<T> value) {
        return value == null ? List.of() : value.stream().filter(Objects::nonNull).toList();
    }
}
