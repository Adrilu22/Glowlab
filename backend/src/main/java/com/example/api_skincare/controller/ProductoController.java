package com.example.api_skincare.controller;

import com.example.api_skincare.model.Producto;
import com.example.api_skincare.model.Categoria;
import com.example.api_skincare.repository.ProductoRepository;
import com.example.api_skincare.repository.CategoriaRepository; // Importante añadir esto

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*") // Permite la conexión desde Firebase o local
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    // Inyectamos ambos repositorios
    public ProductoController(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // GET: Obtener todos los productos para el Front
    @GetMapping
    public List<Producto> obtenerProductos() {
        return productoRepository.findAll();
    }

    // GET: Obtener un producto por su ID
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return productoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST: Crear producto (con validación de categoría para evitar error 500)
    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {
        if (producto.getCategoria() != null && producto.getCategoria().getId() != null) {
            Categoria cat = categoriaRepository.findById(producto.getCategoria().getId()).orElse(null);
            producto.setCategoria(cat);
        }
        return ResponseEntity.ok(productoRepository.save(producto));
    }

    // PUT: Actualizar un producto existente
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @RequestBody Producto producto) {
        if (!productoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        producto.setId(id);
        return ResponseEntity.ok(productoRepository.save(producto));
    }

    // DELETE: Eliminar producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        if (!productoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // RELACIÓN: Obtener la categoría de un producto específico (usado en tu script.js)
    @GetMapping("/{id}/categoria")
    public ResponseEntity<Categoria> obtenerCategoria(@PathVariable Long id) {
        return productoRepository.findById(id)
                .map(p -> ResponseEntity.ok(p.getCategoria()))
                .orElse(ResponseEntity.notFound().build());
    }
}