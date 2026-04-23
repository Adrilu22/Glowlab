package com.example.api_skincare.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rutinas")
public class Rutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    private String nombre;

    @Column(name = "tipo_piel")
    private String tipoPiel;

    private String preocupaciones;

    @Column(name = "pasos_json", columnDefinition = "TEXT")
    private String pasosJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Rutina() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public String getNombre() { return nombre; }
    public String getTipoPiel() { return tipoPiel; }
    public String getPreocupaciones() { return preocupaciones; }
    public String getPasosJson() { return pasosJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTipoPiel(String tipoPiel) { this.tipoPiel = tipoPiel; }
    public void setPreocupaciones(String preocupaciones) { this.preocupaciones = preocupaciones; }
    public void setPasosJson(String pasosJson) { this.pasosJson = pasosJson; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
