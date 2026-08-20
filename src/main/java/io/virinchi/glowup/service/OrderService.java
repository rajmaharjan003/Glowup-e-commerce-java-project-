package io.virinchi.glowup.service;

import io.virinchi.glowup.entity.Order;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    public Order createOrder(Order order) {
        // TODO: save order using your OrderRepository
        return order;
    }
}