package io.virinchi.glowup.repository;

import io.virinchi.glowup.entity.CartItem;
import io.virinchi.glowup.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    Optional<CartItem> findByCartIdAndProduct(Long cartId, Product product);
}