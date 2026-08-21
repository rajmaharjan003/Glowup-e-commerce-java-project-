package io.virinchi.glowup.repository;

import io.virinchi.glowup.entity.Order;
import io.virinchi.glowup.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAtDesc(
            User user
    );
}