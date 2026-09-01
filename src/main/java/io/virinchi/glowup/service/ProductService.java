package io.virinchi.glowup.service;

import io.virinchi.glowup.dto.ProductDto;
import io.virinchi.glowup.entity.Category;
import io.virinchi.glowup.entity.Product;
import io.virinchi.glowup.entity.Review;
import io.virinchi.glowup.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartItemRepository cartItemRepository;
    private final WishlistRepository wishlistRepository;
    private final ReviewRepository reviewRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            CartItemRepository cartItemRepository,
            WishlistRepository wishlistRepository,
            ReviewRepository reviewRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.cartItemRepository = cartItemRepository;
        this.wishlistRepository = wishlistRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> getProductByName(String name) {
        return productRepository.findFirstByNameIgnoreCase(name);
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
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return;
        }

        // 1. Delete cart items with this product
        try {
            cartItemRepository.findAll().stream()
                    .filter(ci -> ci.getProduct() != null && ci.getProduct().getId().equals(id))
                    .forEach(cartItemRepository::delete);
        } catch (Exception e) {
            log.warn("Notice cleaning cart items for product {}: {}", id, e.getMessage());
        }

        // 2. Delete wishlists with this product
        try {
            wishlistRepository.findAll().stream()
                    .filter(w -> w.getProduct() != null && w.getProduct().getId().equals(id))
                    .forEach(wishlistRepository::delete);
        } catch (Exception e) {
            log.warn("Notice cleaning wishlists for product {}: {}", id, e.getMessage());
        }

        // 3. Decouple reviews for this product
        try {
            List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(id);
            for (Review r : reviews) {
                r.setProduct(null);
                reviewRepository.save(r);
            }
        } catch (Exception e) {
            log.warn("Notice decoupling reviews for product {}: {}", id, e.getMessage());
        }

        productRepository.delete(product);
        log.info("Product #{} ({}) deleted successfully", id, product.getName());
    }
}

