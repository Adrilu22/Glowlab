package com.example.api_skincare.controller;

import com.example.api_skincare.config.JwtUtil;
import com.example.api_skincare.model.Usuario;
import com.example.api_skincare.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoints de autenticación.
 *
 *   POST /api/auth/register  — crea una cuenta nueva (rol USER por defecto)
 *   POST /api/auth/login     — valida credenciales y devuelve un token JWT
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder   = passwordEncoder;
        this.jwtUtil           = jwtUtil;
    }

    // -------------------------------------------------------------------------
    // Registro
    // -------------------------------------------------------------------------

    static class RegisterRequest {
        public String nombre;
        public String email;
        public String password;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.email == null || req.email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El email es obligatorio"));
        }
        if (req.password == null || req.password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La contraseña es obligatoria"));
        }
        if (usuarioRepository.existsByEmail(req.email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ya existe una cuenta con ese correo"));
        }

        Usuario u = new Usuario();
        u.setNombre(req.nombre);
        u.setEmail(req.email);
        u.setPasswordHash(passwordEncoder.encode(req.password));
        u.setRol("USER");
        usuarioRepository.save(u);

        String token = jwtUtil.generate(u.getEmail(), u.getRol());
        return ResponseEntity.ok(Map.of(
                "token", token,
                "email", u.getEmail(),
                "nombre", u.getNombre() != null ? u.getNombre() : "",
                "rol", u.getRol()
        ));
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    static class LoginRequest {
        public String email;
        public String password;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req.email == null || req.password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email y contraseña son obligatorios"));
        }

        return usuarioRepository.findByEmail(req.email)
                .map(usuario -> {
                    if (!passwordEncoder.matches(req.password, usuario.getPasswordHash())) {
                        return ResponseEntity.status(401)
                                .body(Map.of("error", "Credenciales incorrectas"));
                    }
                    String token = jwtUtil.generate(usuario.getEmail(), usuario.getRol());
                    return ResponseEntity.ok(Map.of(
                            "token", token,
                            "email", usuario.getEmail(),
                            "nombre", usuario.getNombre() != null ? usuario.getNombre() : "",
                            "rol", usuario.getRol()
                    ));
                })
                .orElse(ResponseEntity.status(401)
                        .body(Map.of("error", "Credenciales incorrectas")));
    }
}
