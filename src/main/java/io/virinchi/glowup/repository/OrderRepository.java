package io.virinchi.glowup.repository;

import io.virinchi.glowup.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}