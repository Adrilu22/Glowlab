package com.example.api_skincare.service;

import com.example.api_skincare.model.Producto;
import com.example.api_skincare.repository.ProductoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    // La API key va en el header x-goog-api-key, NO como query param,
    // para evitar que aparezca en logs de Cloud Run y Prometheus.
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final ProductoRepository productoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${gemini.api-key:}")
    private String apiKey;

    public ChatbotService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public String chat(String userMessage, List<Map<String, String>> historial) {
        if (apiKey == null || apiKey.isBlank()) {
            return "⚠️ El chatbot requiere una clave de Gemini. Configura GEMINI_API_KEY en el entorno.";
        }

        try {
            List<Producto> productos = productoRepository.findAll();
            String systemPrompt = buildSystemPrompt(productos);
            String requestBody = buildRequestBody(systemPrompt, userMessage, historial);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)   // key en header, no en URL
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(response.body());

            if (response.statusCode() != 200) {
                String errorMsg = json.path("error").path("message").asText("sin detalle");
                System.err.println("❌ Gemini " + response.statusCode() + ": " + errorMsg);
                return "Lo siento, tuve un problema al consultar la IA. Intenta de nuevo en unos momentos.";
            }

            return json.path("candidates").path(0)
                    .path("content").path("parts").path(0)
                    .path("text").asText("No pude generar una respuesta en este momento.");

        } catch (Exception e) {
            System.err.println("❌ Error chatbot [" + e.getClass().getSimpleName() + "]: " + e.getMessage());
            return "Lo siento, ocurrió un error al procesar tu consulta.";
        }
    }

    private String buildSystemPrompt(List<Producto> productos) {
        StringBuilder catalogo = new StringBuilder();
        for (Producto p : productos) {
            catalogo.append("• %s (%s) — %s. Tipos de piel: %s. Precio: $%,.0f COP%n"
                    .formatted(p.getNombre(),
                            p.getMarca() != null ? p.getMarca() : "Sin marca",
                            p.getDescripcion() != null ? p.getDescripcion() : "",
                            p.getTiposPiel() != null ? p.getTiposPiel() : "todos",
                            p.getPrecio()));
        }

        return """
                Eres Glow, la asistente virtual de GlowLab, una tienda colombiana especializada en skincare de alta calidad.
                Tu objetivo es guiar a cada cliente en una consulta de skincare personalizada, como lo haría una amiga experta en cuidado de la piel.

                FLUJO CONVERSACIONAL:
                1. Si no conoces el tipo de piel del cliente (seca, grasa, mixta, sensible, normal), pregúntalo amablemente.
                2. Si no conoces sus preocupaciones (acné, manchas, poros, sequedad, brillo, arrugas), pregúntalas.
                3. Con esa información, recomienda 2-3 productos específicos del catálogo adaptados a su perfil.
                4. Sugiere al menos 1 producto complementario que complete la rutina.
                5. Tras hacer las recomendaciones, evalúa si el cliente se beneficiaría de una asesoría personalizada.
                   Si es así, incluye al final de tu mensaje exactamente esta etiqueta: [ASESOR_DISPONIBLE]
                   Solo inclúyela una vez por conversación.

                CATÁLOGO ACTUAL DE GLOWLAB:
                %s

                REGLAS:
                - Responde siempre en español, con tono cálido y cercano. Usa "tú".
                - Recomienda EXCLUSIVAMENTE productos del catálogo anterior. Incluye nombre, marca y precio en COP.
                - Sé concisa: máximo 4 párrafos o 5 puntos. No repitas información ya dada.
                - Usa emojis ocasionalmente para un tono amigable ✨
                - No incluyas [ASESOR_DISPONIBLE] si ya la pusiste antes en la conversación.
                """.formatted(catalogo.toString());
    }

    private String buildRequestBody(String systemPrompt, String userMessage,
                                    List<Map<String, String>> historial) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();

        // Instrucción de sistema
        ObjectNode systemInstruction = objectMapper.createObjectNode();
        ArrayNode sysParts = objectMapper.createArrayNode();
        ObjectNode sysPart = objectMapper.createObjectNode();
        sysPart.put("text", systemPrompt);
        sysParts.add(sysPart);
        systemInstruction.set("parts", sysParts);
        body.set("system_instruction", systemInstruction);

        // Historial + mensaje actual
        ArrayNode contents = objectMapper.createArrayNode();

        for (Map<String, String> entry : historial) {
            String role    = entry.getOrDefault("role", "user");
            String content = entry.getOrDefault("content", "");
            if (!content.isBlank()) {
                String geminiRole = role.equals("assistant") ? "model" : "user";
                ObjectNode turn = objectMapper.createObjectNode();
                turn.put("role", geminiRole);
                ArrayNode parts = objectMapper.createArrayNode();
                ObjectNode part = objectMapper.createObjectNode();
                part.put("text", content);
                parts.add(part);
                turn.set("parts", parts);
                contents.add(turn);
            }
        }

        // Mensaje actual del usuario
        ObjectNode currentTurn = objectMapper.createObjectNode();
        currentTurn.put("role", "user");
        ArrayNode currentParts = objectMapper.createArrayNode();
        ObjectNode currentPart = objectMapper.createObjectNode();
        currentPart.put("text", userMessage);
        currentParts.add(currentPart);
        currentTurn.set("parts", currentParts);
        contents.add(currentTurn);

        body.set("contents", contents);

        // Configuración de generación
        ObjectNode generationConfig = objectMapper.createObjectNode();
        generationConfig.put("maxOutputTokens", 1500);
        body.set("generationConfig", generationConfig);

        return objectMapper.writeValueAsString(body);
    }
}
