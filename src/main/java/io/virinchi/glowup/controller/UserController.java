package io.virinchi.glowup.controller;

import io.virinchi.glowup.entity.*;
import io.virinchi.glowup.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    public UserController(
            UserRepository userRepository,
            CartRepository cartRepository,
            WishlistRepository wishlistRepository,
            ReviewRepository reviewRepository,
            OrderRepository orderRepository
    ) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.wishlistRepository = wishlistRepository;
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<User> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        String newRole = body != null ? body.get("role") : "CUSTOMER";
        user.setRole(newRole);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "User not found with ID: " + id,
                    "status", "ERROR"
            ));
        }

        String userEmail = user.getEmail();

        // 1. Clean up Carts
        try {
            cartRepository.findByUser(user).ifPresent(cartRepository::delete);
        } catch (Exception e) {
            log.warn("Could not delete user cart: {}", e.getMessage());
        }

        // 2. Clean up Wishlists
        try {
            wishlistRepository.deleteByUser(user);
        } catch (Exception e) {
            log.warn("Could not delete user wishlist: {}", e.getMessage());
        }

        // 3. Decouple Reviews (keep customer review text on product but set user to null)
        try {
            List<Review> userReviews = reviewRepository.findByUser(user);
            for (Review r : userReviews) {
                r.setUser(null);
                reviewRepository.save(r);
            }
        } catch (Exception e) {
            log.warn("Could not decouple user reviews: {}", e.getMessage());
        }

        // 4. Decouple Orders (keep transaction and items intact in DB history, unlink user reference)
        try {
            List<Order> userOrders = orderRepository.findByUser(user);
            for (Order o : userOrders) {
                o.setUser(null);
                orderRepository.save(o);
            }
        } catch (Exception e) {
            log.warn("Could not decouple user orders: {}", e.getMessage());
        }

        // 5. Delete User entity
        userRepository.delete(user);
        log.info("User deleted successfully: id={}, email={}", id, userEmail);

        return ResponseEntity.ok(Map.of(
                "message", "User '" + userEmail + "' deleted successfully.",
                "status", "SUCCESS",
                "deletedUserId", id
        ));
    }

    @DeleteMapping("/email/{email}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteUserByEmail(@PathVariable String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "User not found with email: " + email,
                    "status", "ERROR"
            ));
        }
        return deleteUser(user.getId());
    }
}

