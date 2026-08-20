package io.virinchi.glowup.repository;

import io.virinchi.glowup.entity.Cart;
import io.virinchi.glowup.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);
}