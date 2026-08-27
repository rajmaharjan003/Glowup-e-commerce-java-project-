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

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable String id) {
        Order order = orderService.resolveOrder(id);
        if (order != null) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/track/{identifier}")
    public ResponseEntity<Map<String, Object>> getTrackingDetails(@PathVariable String identifier) {
        Order order = orderService.resolveOrder(identifier);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("order", order);
        response.put("delivery", orderService.getDeliveryForOrder(order.getId()));
        response.put("payment", orderService.getPaymentForOrder(order.getId()));
        return ResponseEntity.ok(response);
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
            @PathVariable String id,
            @RequestBody Map<String, String> body
    ) {
        String status = body != null ? body.get("status") : "PENDING";
        String reason = body != null ? body.get("reason") : null;
        
        Order resolved = orderService.resolveOrder(id);
        if (resolved == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(orderService.updateOrderStatus(resolved.getId(), status, reason));
    }
}