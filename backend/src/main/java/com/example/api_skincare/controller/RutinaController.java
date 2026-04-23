package com.example.api_skincare.controller;

import com.example.api_skincare.model.Rutina;
import com.example.api_skincare.repository.RutinaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rutinas")
public class RutinaController {

    private final RutinaRepository rutinaRepository;

    public RutinaController(RutinaRepository rutinaRepository) {
        this.rutinaRepository = rutinaRepository;
    }

    @GetMapping
    public List<Rutina> obtenerRutinas() {
        return rutinaRepository.findAll();
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Rutina> obtenerPorUsuario(@PathVariable Long usuarioId) {
        return rutinaRepository.findByUsuarioId(usuarioId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rutina> obtenerPorId(@PathVariable Long id) {
        return rutinaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Rutina> crearRutina(@RequestBody Rutina rutina) {
        return ResponseEntity.ok(rutinaRepository.save(rutina));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRutina(@PathVariable Long id) {
        if (!rutinaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        rutinaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
