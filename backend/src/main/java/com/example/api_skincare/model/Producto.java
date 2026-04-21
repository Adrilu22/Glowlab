package com.example.api_skincare.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String marca;

    private String descripcion;

    private double precio;

    private String tiposPiel;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
}
