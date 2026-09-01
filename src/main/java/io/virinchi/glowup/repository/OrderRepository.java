package io.virinchi.glowup.repository;

import io.virinchi.glowup.entity.Order;
import io.virinchi.glowup.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    List<Order> findByUser(User user);

    List<Order> findAllByOrderByCreatedAtDesc();
}