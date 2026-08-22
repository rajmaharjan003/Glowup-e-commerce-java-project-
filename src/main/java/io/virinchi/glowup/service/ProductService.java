package io.virinchi.glowup.service;

import io.virinchi.glowup.dto.ProductDto;
import io.virinchi.glowup.entity.Category;
import io.virinchi.glowup.entity.Product;
import io.virinchi.glowup.repository.CategoryRepository;
import io.virinchi.glowup.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> getProductByName(String name) {
        return productRepository.findByNameIgnoreCase(name);
    }

    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.findByNameContainingIgnoreCase(query.trim());
    }

    @Transactional
    public Product createOrUpdateProduct(ProductDto dto) {
        Product product = null;
        if (dto.getId() != null) {
            product = productRepository.findById(dto.getId()).orElse(new Product());
        } else {
            product = new Product();
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setDiscount(dto.getDiscount());
        product.setStock(dto.getStock());
        product.setImage(dto.getImage());
        product.setBrand(dto.getBrand());
        product.setRating(dto.getRating() > 0 ? dto.getRating() : 5.0);
        product.setFeatured(dto.isFeatured());
        product.setFlashSale(dto.isFlashSale());

        if (dto.getCategory() != null && !dto.getCategory().trim().isEmpty()) {
            Category category = categoryRepository.findByName(dto.getCategory().trim()).orElse(null);
            if (category == null) {
                category = new Category();
                category.setName(dto.getCategory().trim());
                category = categoryRepository.save(category);
            }
            product.setCategory(category);
        }

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
