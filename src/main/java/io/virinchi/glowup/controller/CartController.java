package io.virinchi.glowup.controller;

import io.virinchi.glowup.dto.AddCartRequest;
import io.virinchi.glowup.dto.CartResponse;
import io.virinchi.glowup.dto.UpdateCartRequest;
import io.virinchi.glowup.service.CartService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;


    public CartController(
            CartService cartService
    ) {
        this.cartService = cartService;
    }


    // ==========================================
    // ADD TO CART
    // ==========================================

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(
            @RequestBody AddCartRequest request
    ) {

        if (request.getEmail() == null ||
                request.getEmail().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .build();
        }

        CartResponse response =
                cartService.addToCart(
                        request.getEmail(),
                        request
                );

        return ResponseEntity.ok(response);
    }


    // ==========================================
    // GET CART
    // ==========================================

    @GetMapping("/{email}")
    public ResponseEntity<CartResponse> getCart(
            @PathVariable String email
    ) {

        CartResponse response =
                cartService.getCart(email);

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

        if (request.getEmail() == null ||
                request.getEmail().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .build();
        }

        CartResponse response =
                cartService.updateQuantity(
                        request.getEmail(),
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

        CartResponse response =
                cartService.removeItem(
                        email,
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

        CartResponse response =
                cartService.clearCart(email);

        return ResponseEntity.ok(response);
    }
}