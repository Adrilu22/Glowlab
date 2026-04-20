package com.example.api_skincare.controller;

import com.example.api_skincare.model.Compra;
import com.example.api_skincare.model.DetalleCompra;
import com.example.api_skincare.repository.CompraRepository;
import com.example.api_skincare.repository.DetalleCompraRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
@CrossOrigin("*")
public class CompraController {

    private final CompraRepository compraRepo;
    private final DetalleCompraRepository detalleRepo;

    public CompraController(CompraRepository compraRepo, DetalleCompraRepository detalleRepo) {
        this.compraRepo = compraRepo;
        this.detalleRepo = detalleRepo;
    }

    @PostMapping
    public String guardarCompra(@RequestBody List<java.util.Map<String, Object>> detalles) {
        try {
            System.out.println("📦 Detalles recibidos: " + detalles);

            Compra compra = compraRepo.save(new Compra());
            System.out.println("✅ Compra creada con ID: " + compra.getId());

            for (java.util.Map<String, Object> d : detalles) {
                Long productoId = ((Number) d.get("productoId")).longValue();
                System.out.println("💾 Guardando productoId: " + productoId);

                DetalleCompra detalle = new DetalleCompra();
                detalle.setProductoId(productoId);
                detalle.setCompraId(compra.getId());

                detalleRepo.save(detalle);
                System.out.println("✅ Detalle guardado");
            }

            return "Compra guardada correctamente ✅";

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}