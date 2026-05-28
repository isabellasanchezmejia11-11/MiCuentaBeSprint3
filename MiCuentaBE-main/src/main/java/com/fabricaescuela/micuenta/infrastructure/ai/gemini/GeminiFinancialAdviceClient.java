package com.fabricaescuela.micuenta.infrastructure.ai.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fabricaescuela.micuenta.application.dto.response.FinancialAdviceAiContent;
import com.fabricaescuela.micuenta.application.exception.GeminiServiceException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GeminiFinancialAdviceClient {

private final ObjectMapper objectMapper = new ObjectMapper();
private final HttpClient httpClient;
private final String apiKey;
private final String model;
private final String apiBaseUrl;

public GeminiFinancialAdviceClient(
        @Value("${gemini.api-key:}") String apiKey,
        @Value("${gemini.model:gemini-2.5-flash}") String model,
        @Value("${gemini.api-base-url:https://generativelanguage.googleapis.com/v1beta}") String apiBaseUrl
) {
    this.apiKey = apiKey;
    this.model = model;
    this.apiBaseUrl = apiBaseUrl;
    this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();
}

public FinancialAdviceAiContent generateAdvice(String financialContextJson) {

    if (!StringUtils.hasText(apiKey)) {
        throw new GeminiServiceException(
                "Configura la API Key de Gemini para activar el asesor financiero con IA."
        );
    }

    try {

        String payload = objectMapper.writeValueAsString(
                buildRequestBody(financialContextJson)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiBaseUrl + "/models/" + model + ":generateContent"))
                .timeout(Duration.ofSeconds(45))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {

            String message;

            switch (response.statusCode()) {

                case 403:
                    message = "La API de Gemini no está autorizada.";
                    break;

                case 429:
                    message = "Se alcanzó el límite de solicitudes a Gemini. Intenta nuevamente más tarde.";
                    break;

                case 500:
                    message = "Gemini presentó un error interno.";
                    break;

                case 503:
                    message = "El asesor financiero está temporalmente ocupado. Intenta nuevamente en unos segundos.";
                    break;

                default:
                    message = "No se pudo generar el análisis financiero.";
            }

            throw new GeminiServiceException(message);
        }

        String rawJson = extractText(response.body());

        if (!StringUtils.hasText(rawJson)) {
            return FinancialAdviceAiContent.fallback(
                    "No fue posible obtener una respuesta útil de Gemini en este momento."
            );
        }

        return objectMapper.readValue(
                cleanJson(rawJson),
                FinancialAdviceAiContent.class
        );

    } catch (IOException ex) {

        throw new GeminiServiceException(
                "No fue posible interpretar la respuesta de Gemini."
        );

    } catch (InterruptedException ex) {

        Thread.currentThread().interrupt();

        throw new GeminiServiceException(
                "La solicitud a Gemini fue interrumpida."
        );
    }
}

private Map<String, Object> buildRequestBody(String financialContextJson) {

    String prompt = """
            Actúa como un asesor financiero educativo dentro de una app juvenil de finanzas personales en Colombia.

            Tu tarea:
            - Analizar únicamente los datos entregados.
            - No inventar ingresos, gastos, deudas, presupuestos ni movimientos.
            - Generar recomendaciones concretas, empáticas y accionables.
            - Ayudar al usuario a entender hábitos de gasto, ahorro, presupuesto y movimientos recientes.
            - Evitar prometer resultados financieros.
            - Evitar asesoría de inversión personalizada.
            - Evitar recomendar endeudamiento riesgoso.
            - Usar español natural, claro y breve.
            - Responder únicamente con JSON válido.

            Datos financieros del usuario:
            %s
            """.formatted(financialContextJson);

    Map<String, Object> body = new LinkedHashMap<>();

    body.put("contents", List.of(Map.of(
            "role", "user",
            "parts", List.of(Map.of("text", prompt))
    )));

    Map<String, Object> generationConfig = new LinkedHashMap<>();
    generationConfig.put("temperature", 0.35);
    generationConfig.put("responseMimeType", "application/json");
    generationConfig.put("responseSchema", responseSchema());

    body.put("generationConfig", generationConfig);

    return body;
}

private Map<String, Object> responseSchema() {

    Map<String, Object> insight = new LinkedHashMap<>();

    insight.put("type", "object");

    insight.put("properties", Map.of(
            "title", stringSchema(),
            "description", stringSchema(),
            "severity", enumSchema(List.of("GOOD", "INFO", "WARNING", "RISK")),
            "icon", enumSchema(List.of("savings", "alert", "chart", "budget", "movement"))
    ));

    insight.put("required", List.of(
            "title",
            "description",
            "severity",
            "icon"
    ));

    Map<String, Object> actionItem = new LinkedHashMap<>();

    actionItem.put("type", "object");

    actionItem.put("properties", Map.of(
            "title", stringSchema(),
            "description", stringSchema(),
            "priority", enumSchema(List.of("HIGH", "MEDIUM", "LOW")),
            "estimatedImpact", stringSchema()
    ));

    actionItem.put("required", List.of(
            "title",
            "description",
            "priority",
            "estimatedImpact"
    ));

    Map<String, Object> categoryRecommendation = new LinkedHashMap<>();

    categoryRecommendation.put("type", "object");

    categoryRecommendation.put("properties", Map.of(
            "categoryName", stringSchema(),
            "currentAmount", numberSchema(),
            "percentage", numberSchema(),
            "status", enumSchema(List.of("OK", "WATCH", "OVERSPENT")),
            "diagnosis", stringSchema(),
            "recommendation", stringSchema(),
            "suggestedLimit", numberSchema()
    ));

    categoryRecommendation.put("required", List.of(
            "categoryName",
            "currentAmount",
            "percentage",
            "status",
            "diagnosis",
            "recommendation",
            "suggestedLimit"
    ));

    Map<String, Object> root = new LinkedHashMap<>();

    root.put("type", "object");

    root.put("properties", Map.of(
            "headline", stringSchema(),
            "summary", stringSchema(),
            "healthStatus", enumSchema(List.of("POSITIVE", "BALANCED", "ATTENTION", "RISK")),
            "insights", arraySchema(insight),
            "actionItems", arraySchema(actionItem),
            "categoryRecommendations", arraySchema(categoryRecommendation),
            "alerts", arraySchema(stringSchema()),
            "disclaimer", stringSchema()
    ));

    root.put("required", List.of(
            "headline",
            "summary",
            "healthStatus",
            "insights",
            "actionItems",
            "categoryRecommendations",
            "alerts",
            "disclaimer"
    ));

    return root;
}

private Map<String, Object> stringSchema() {
    return Map.of("type", "string");
}

private Map<String, Object> numberSchema() {
    return Map.of("type", "number");
}

private Map<String, Object> enumSchema(List<String> values) {
    return Map.of(
            "type", "string",
            "enum", values
    );
}

private Map<String, Object> arraySchema(Map<String, Object> itemSchema) {
    return Map.of(
            "type", "array",
            "items", itemSchema
    );
}

private String extractText(String responseBody) throws IOException {

    JsonNode root = objectMapper.readTree(responseBody);

    JsonNode textNode = root
            .path("candidates")
            .path(0)
            .path("content")
            .path("parts")
            .path(0)
            .path("text");

    if (!textNode.isMissingNode() && textNode.isTextual()) {
        return textNode.asText();
    }

    JsonNode finishReason = root
            .path("candidates")
            .path(0)
            .path("finishReason");

    if (!finishReason.isMissingNode() && finishReason.isTextual()) {
        throw new GeminiServiceException(
                "Gemini no devolvió texto. Motivo: " + finishReason.asText()
        );
    }

    JsonNode blockReason = root
            .path("promptFeedback")
            .path("blockReason");

    if (!blockReason.isMissingNode() && blockReason.isTextual()) {
        throw new GeminiServiceException(
                "Gemini bloqueó la respuesta: " + blockReason.asText()
        );
    }

    return null;
}

private String cleanJson(String value) {

    String cleaned = value.trim();

    if (cleaned.startsWith("```")) {

        cleaned = cleaned
                .replaceFirst("^```json", "")
                .replaceFirst("^```", "")
                .replaceFirst("```$", "")
                .trim();
    }

    return cleaned;
}
}