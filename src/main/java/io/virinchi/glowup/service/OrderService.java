package io.virinchi.glowup.service;

import io.virinchi.glowup.entity.Order;
import io.virinchi.glowup.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final EmailService emailService;

    public OrderService(
            OrderRepository orderRepository,
            EmailService emailService) {

        this.orderRepository = orderRepository;
        this.emailService = emailService;
    }

    public Order createOrder(Order order) {

        // Set initial status
        order.setStatus("CONFIRMED");

        // Save order first
        Order savedOrder = orderRepository.save(order);

        // Send confirmation email
        emailService.sendOrderConfirmation(
                savedOrder.getCustomerEmail(),
                savedOrder.getCustomerName(),
                savedOrder.getId(),
                savedOrder.getTotalAmount()
        );

        return savedOrder;
    }
}