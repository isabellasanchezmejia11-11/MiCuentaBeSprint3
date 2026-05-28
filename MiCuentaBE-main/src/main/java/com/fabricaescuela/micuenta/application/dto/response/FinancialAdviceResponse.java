package com.fabricaescuela.micuenta.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FinancialAdviceResponse(
        String period,
        String headline,
        String summary,
        String healthStatus,
        BigDecimal monthlyIncome,
        BigDecimal monthlyExpense,
        BigDecimal monthlyNet,
        BigDecimal savingsRate,
        List<FinancialAdviceAiContent.FinancialInsightResponse> insights,
        List<FinancialAdviceAiContent.FinancialActionItemResponse> actionItems,
        List<FinancialAdviceAiContent.FinancialCategoryRecommendationResponse> categoryRecommendations,
        List<String> alerts,
        String disclaimer,
        LocalDateTime generatedAt
) {}
