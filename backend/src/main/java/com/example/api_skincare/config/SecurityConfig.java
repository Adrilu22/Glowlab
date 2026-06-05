package com.example.api_skincare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configura Spring Security para la API REST de GlowLab.
 *
 * Reglas:
 *  - POST /api/auth/** → público (login y registro)
 *  - GET  /api/**      → público (lectura libre)
 *  - GET  /metrics     → público (Prometheus scrape)
 *  - GET  /actuator/** → público (health checks)
 *  - POST, PUT, DELETE /api/** → requieren token JWT válido
 *  - Todo lo demás (frontend estático) → permitido
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ----- Rutas siempre públicas (van primero — más específicas) -----

                // Login y registro
                .requestMatchers("/api/auth/**").permitAll()

                // Registro de usuario directo (compatibilidad con flujo anterior)
                .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()

                // Chatbot público (no requiere login)
                .requestMatchers(HttpMethod.POST, "/api/chatbot").permitAll()

                // Prometheus y Actuator
                .requestMatchers("/metrics", "/actuator/**").permitAll()

                // Frontend estático
                .requestMatchers("/", "/index.html", "/*.css", "/*.js", "/favicon.ico").permitAll()

                // Lectura libre en todos los endpoints
                .requestMatchers(HttpMethod.GET, "/api/**").permitAll()

                // ----- Escritura requiere token JWT (reglas generales al final) -----
                .requestMatchers(HttpMethod.POST,   "/api/**").authenticated()
                .requestMatchers(HttpMethod.PUT,    "/api/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/**").authenticated()

                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
