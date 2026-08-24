package io.virinchi.glowup.service;

import io.virinchi.glowup.dto.WishlistRequest;
import io.virinchi.glowup.dto.WishlistResponse;
import io.virinchi.glowup.entity.Product;
import io.virinchi.glowup.entity.User;
import io.virinchi.glowup.entity.Wishlist;
import io.virinchi.glowup.repository.ProductRepository;
import io.virinchi.glowup.repository.UserRepository;
import io.virinchi.glowup.repository.WishlistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WishlistService {

    private static final Logger log = LoggerFactory.getLogger(WishlistService.class);

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public WishlistService(
            WishlistRepository wishlistRepository,
            UserRepository userRepository,
            ProductRepository productRepository
    ) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public List<WishlistResponse> getUserWishlist(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ArrayList<>();
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
        if (user == null) {
            return new ArrayList<>();
        }

        List<Wishlist> items = wishlistRepository.findByUserOrderByCreatedAtDesc(user);
        List<WishlistResponse> responses = new ArrayList<>();

        for (Wishlist w : items) {
            Product p = w.getProduct();
            if (p != null) {
                responses.add(new WishlistResponse(
                        w.getId(),
                        p.getId(),
                        p.getName(),
                        p.getPrice(),
                        p.getDiscount(),
                        p.getImage(),
                        p.getCategory() != null ? p.getCategory().getName() : "General",
                        p.getBrand(),
                        p.getRating(),
                        w.getCreatedAt()
                ));
            }
        }
        return responses;
    }

    @Transactional
    public boolean toggleWishlist(WishlistRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("User email is required");
        }

        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFullName(email.split("@")[0]);
            user.setPhoneNumber("9800000000");
            user.setPassword("guest_pass");
            user.setRole("CUSTOMER");
            user.setVerified(true);
            user = userRepository.save(user);
        }

        Product product = null;
        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId()).orElse(null);
        }
        if (product == null && request.getProductName() != null && !request.getProductName().trim().isEmpty()) {
            product = productRepository.findByNameIgnoreCase(request.getProductName().trim()).orElse(null);
        }

        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        Optional<Wishlist> existing = wishlistRepository.findByUserAndProduct(user, product);
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            log.info("Removed product {} from wishlist for user {}", product.getName(), email);
            return false; // Removed
        } else {
            Wishlist wishlist = new Wishlist();
            wishlist.setUser(user);
            wishlist.setProduct(product);
            wishlistRepository.save(wishlist);
            log.info("Added product {} to wishlist for user {}", product.getName(), email);
            return true; // Added
        }
    }

    @Transactional
    public WishlistResponse addToWishlist(WishlistRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("User email is required");
        }

        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFullName(email.split("@")[0]);
            user.setPhoneNumber("9800000000");
            user.setPassword("guest_pass");
            user.setRole("CUSTOMER");
            user.setVerified(true);
            user = userRepository.save(user);
        }

        Product product = null;
        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId()).orElse(null);
        }
        if (product == null && request.getProductName() != null && !request.getProductName().trim().isEmpty()) {
            product = productRepository.findByNameIgnoreCase(request.getProductName().trim()).orElse(null);
        }

        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        Optional<Wishlist> existing = wishlistRepository.findByUserAndProduct(user, product);
        Wishlist wishlist = existing.orElse(null);

        if (wishlist == null) {
            wishlist = new Wishlist();
            wishlist.setUser(user);
            wishlist.setProduct(product);
            wishlist = wishlistRepository.save(wishlist);
        }

        return new WishlistResponse(
                wishlist.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDiscount(),
                product.getImage(),
                product.getCategory() != null ? product.getCategory().getName() : "General",
                product.getBrand(),
                product.getRating(),
                wishlist.getCreatedAt()
        );
    }

    @Transactional
    public boolean removeFromWishlist(String email, Long productId) {
        if (email == null || productId == null) return false;
        User user = userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);
        if (user != null && product != null) {
            wishlistRepository.deleteByUserAndProduct(user, product);
            return true;
        }
        return false;
    }

    public boolean isInWishlist(String email, Long productId) {
        if (email == null || productId == null) return false;
        User user = userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);
        if (user != null && product != null) {
            return wishlistRepository.existsByUserAndProduct(user, product);
        }
        return false;
    }
}
