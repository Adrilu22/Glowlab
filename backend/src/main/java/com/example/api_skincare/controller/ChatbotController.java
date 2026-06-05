package com.example.api_skincare.controller;

import com.example.api_skincare.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    static class ChatRequest {
        public String mensaje = "";
        public List<Map<String, String>> historial = new ArrayList<>();
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody ChatRequest req) {
        if (req.mensaje.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El campo 'mensaje' es obligatorio"));
        }
        String respuesta = chatbotService.chat(req.mensaje, req.historial);
        return ResponseEntity.ok(Map.of("respuesta", respuesta));
    }
}
