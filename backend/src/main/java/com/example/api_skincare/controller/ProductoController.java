package com.example.api_skincare.controller;

import com.example.api_skincare.model.Categoria;
import com.example.api_skincare.model.Producto;
import com.example.api_skincare.repository.CategoriaRepository;
import com.example.api_skincare.repository.ProductoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin("*")
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoController(ProductoRepository productoRepository,
                              CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // GET /api/productos?categoriaId=X
    @GetMapping
    public List<ProductoResponse> obtenerProductos(
            @RequestParam(required = false) Long categoriaId) {
        List<Producto> productos = categoriaId != null
                ? productoRepository.findByCategoriaId(categoriaId)
                : productoRepository.findAll();
        return productos.stream().map(ProductoResponse::from).toList();
    }

    // GET /api/productos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerPorId(@PathVariable Long id) {
        return productoRepository.findById(id)
                .map(p -> ResponseEntity.ok(ProductoResponse.from(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/productos/{id}/categoria
    @GetMapping("/{id}/categoria")
    public ResponseEntity<Categoria> obtenerCategoria(@PathVariable Long id) {
        return productoRepository.findById(id)
                .map(p -> ResponseEntity.ok(p.getCategoria()))
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/productos — valida que la categoría exista
    @PostMapping
    public ResponseEntity<?> crearProducto(@RequestBody Producto producto) {
        String error = validarCategoria(producto.getCategoria());
        if (error != null) return ResponseEntity.badRequest().body(error);
        return ResponseEntity.ok(ProductoResponse.from(productoRepository.save(producto)));
    }

    // PUT /api/productos/{id} — valida categoría y actualiza campo a campo
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Long id,
                                                 @RequestBody Producto datos) {
        String error = validarCategoria(datos.getCategoria());
        if (error != null) return ResponseEntity.badRequest().body(error);

        return productoRepository.findById(id)
                .map(existing -> {
                    existing.setNombre(datos.getNombre());
                    existing.setMarca(datos.getMarca());
                    existing.setDescripcion(datos.getDescripcion());
                    existing.setPrecio(datos.getPrecio());
                    existing.setTiposPiel(datos.getTiposPiel());
                    existing.setCategoria(datos.getCategoria());
                    return (ResponseEntity<?>) ResponseEntity.ok(
                            ProductoResponse.from(productoRepository.save(existing)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/productos/{id} — devuelve 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        if (!productoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private String validarCategoria(Categoria categoria) {
        if (categoria == null || categoria.getId() == null) {
            return "La categoría es obligatoria";
        }
        if (!categoriaRepository.existsById(categoria.getId())) {
            return "La categoría con id " + categoria.getId() + " no existe";
        }
        return null;
    }

    // DTO interno: convierte tiposPiel (String CSV) → List<String>
    record ProductoResponse(Long id, String nombre, String marca, String descripcion,
                            double precio, List<String> tiposPiel, Categoria categoria) {
        static ProductoResponse from(Producto p) {
            List<String> tipos = (p.getTiposPiel() == null || p.getTiposPiel().isBlank())
                    ? List.of()
                    : List.of(p.getTiposPiel().split(",\\s*"));
            return new ProductoResponse(p.getId(), p.getNombre(), p.getMarca(),
                    p.getDescripcion(), p.getPrecio(), tipos, p.getCategoria());
        }
    }
}
