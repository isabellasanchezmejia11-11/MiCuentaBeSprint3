package com.fabricaescuela.micuenta.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contenido estructurado que Gemini debe devolver.
 * No incluye los totales calculados por el sistema porque esos datos deben venir del backend,
 * no del modelo de IA.
 */
public record FinancialAdviceAiContent(
        String headline,
        String summary,
        String healthStatus,
        List<FinancialInsightResponse> insights,
        List<FinancialActionItemResponse> actionItems,
        List<FinancialCategoryRecommendationResponse> categoryRecommendations,
        List<String> alerts,
        String disclaimer
) {
    public static FinancialAdviceAiContent fallback(String message) {
        return new FinancialAdviceAiContent(
                "Tu análisis financiero está listo",
                message,
                "BALANCED",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "Estas recomendaciones son educativas y no reemplazan asesoría financiera profesional."
        );
    }

    public record FinancialInsightResponse(
            String title,
            String description,
            String severity,
            String icon
    ) {}

    public record FinancialActionItemResponse(
            String title,
            String description,
            String priority,
            String estimatedImpact
    ) {}

    public record FinancialCategoryRecommendationResponse(
            String categoryName,
            BigDecimal currentAmount,
            BigDecimal percentage,
            String status,
            String diagnosis,
            String recommendation,
            BigDecimal suggestedLimit
    ) {}
}
