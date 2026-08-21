package io.virinchi.glowup.controller;

import io.virinchi.glowup.dto.CreateOrderRequest;
import io.virinchi.glowup.entity.Order;
import io.virinchi.glowup.service.OrderService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping({"", "/place"})
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable String email) {
        return ResponseEntity.ok(orderService.getUserOrders(email));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String status = body != null ? body.get("status") : "PENDING";
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
}