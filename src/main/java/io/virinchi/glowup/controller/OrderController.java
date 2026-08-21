package io.virinchi.glowup.controller;

import io.virinchi.glowup.dto.CreateOrderRequest;
import io.virinchi.glowup.entity.Order;
import io.virinchi.glowup.service.OrderService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;


    public OrderController(
            OrderService orderService
    ) {

        this.orderService =
                orderService;
    }


    @PostMapping
    public ResponseEntity<Order>
    createOrder(
            @RequestBody
            CreateOrderRequest request
    ) {

        return ResponseEntity.ok(
                orderService.createOrder(
                        request
                )
        );
    }


    @GetMapping("/user/{email}")
    public ResponseEntity<List<Order>>
    getOrders(
            @PathVariable String email
    ) {

        return ResponseEntity.ok(
                orderService.getUserOrders(
                        email
                )
        );
    }
}