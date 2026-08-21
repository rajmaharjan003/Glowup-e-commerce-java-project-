package io.virinchi.glowup.controller;

import io.virinchi.glowup.dto.AddCartRequest;
import io.virinchi.glowup.dto.CartResponse;
import io.virinchi.glowup.dto.UpdateCartRequest;
import io.virinchi.glowup.service.CartService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // ==========================================
    // ADD TO CART
    // ==========================================
    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(
            @RequestBody AddCartRequest request
    ) {
        if (request == null) {
            log.warn("Empty add to cart request");
            return ResponseEntity.badRequest().build();
        }

        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            log.warn("Missing email in add to cart request for product: {}", request.getProductName());
            return ResponseEntity.badRequest().build();
        }

        try {
            CartResponse response = cartService.addToCart(email.trim().toLowerCase(), request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error adding to cart: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ==========================================
    // GET CART
    // ==========================================
    @GetMapping("/{email}")
    public ResponseEntity<CartResponse> getCart(
            @PathVariable String email
    ) {
        CartResponse response = cartService.getCart(email.trim().toLowerCase());
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // UPDATE CART ITEM
    // ==========================================
    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartResponse> updateCart(
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartRequest request
    ) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        CartResponse response = cartService.updateQuantity(
                request.getEmail().trim().toLowerCase(),
                cartItemId,
                request
        );

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // REMOVE CART ITEM
    // ==========================================
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable Long cartItemId,
            @RequestParam String email
    ) {
        CartResponse response = cartService.removeItem(
                email.trim().toLowerCase(),
                cartItemId
        );

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // CLEAR CART
    // ==========================================
    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(
            @RequestParam String email
    ) {
        CartResponse response = cartService.clearCart(email.trim().toLowerCase());
        return ResponseEntity.ok(response);
    }
}