package com.example.api_skincare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "detalle_compra")
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productoId;
    private Long compraId;

    public DetalleCompra() {}

    // GETTERS
    public Long getId() {
        return id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public Long getCompraId() {
        return compraId;
    }

    // SETTERS
    public void setId(Long id) {
        this.id = id;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public void setCompraId(Long compraId) {
        this.compraId = compraId;
    }
}