package io.virinchi.glowup.controller;

import io.virinchi.glowup.dto.WishlistRequest;
import io.virinchi.glowup.dto.WishlistResponse;
import io.virinchi.glowup.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping("/{email}")
    public ResponseEntity<List<WishlistResponse>> getUserWishlist(@PathVariable String email) {
        return ResponseEntity.ok(wishlistService.getUserWishlist(email));
    }

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleWishlist(@RequestBody WishlistRequest request) {
        boolean added = wishlistService.toggleWishlist(request);
        Map<String, Object> result = new HashMap<>();
        result.put("wishlisted", added);
        result.put("message", added ? "Product added to wishlist" : "Product removed from wishlist");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/add")
    public ResponseEntity<WishlistResponse> addToWishlist(@RequestBody WishlistRequest request) {
        return ResponseEntity.ok(wishlistService.addToWishlist(request));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeFromWishlist(
            @RequestParam String email,
            @RequestParam Long productId
    ) {
        boolean removed = wishlistService.removeFromWishlist(email, productId);
        Map<String, Object> result = new HashMap<>();
        result.put("removed", removed);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkWishlist(
            @RequestParam String email,
            @RequestParam Long productId
    ) {
        boolean inWishlist = wishlistService.isInWishlist(email, productId);
        Map<String, Object> result = new HashMap<>();
        result.put("inWishlist", inWishlist);
        return ResponseEntity.ok(result);
    }
}
