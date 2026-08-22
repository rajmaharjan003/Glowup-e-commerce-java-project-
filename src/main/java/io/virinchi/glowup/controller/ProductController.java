package io.virinchi.glowup.controller;

import io.virinchi.glowup.dto.ProductDto;
import io.virinchi.glowup.entity.Product;
import io.virinchi.glowup.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(productService.searchProducts(q));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody ProductDto dto) {
        return ResponseEntity.ok(productService.createOrUpdateProduct(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody ProductDto dto) {
        dto.setId(id);
        return ResponseEntity.ok(productService.createOrUpdateProduct(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
