package io.virinchi.glowup.service;

import io.virinchi.glowup.dto.CreateOrderRequest;
import io.virinchi.glowup.entity.*;
import io.virinchi.glowup.repository.*;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final DeliveryRepository deliveryRepository;
    private final EmailService emailService;


    public OrderService(
            UserRepository userRepository,
            CartRepository cartRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            DeliveryRepository deliveryRepository,
            EmailService emailService
    ) {

        this.userRepository =
                userRepository;

        this.cartRepository =
                cartRepository;

        this.productRepository =
                productRepository;

        this.orderRepository =
                orderRepository;

        this.paymentRepository =
                paymentRepository;

        this.deliveryRepository =
                deliveryRepository;

        this.emailService =
                emailService;
    }


    @Transactional
    public Order createOrder(
            CreateOrderRequest request
    ) {

        User user =
                userRepository
                        .findByEmail(
                                request.getEmail()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );


        Cart cart =
                cartRepository
                        .findByUser(user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cart is empty"
                                )
                        );


        if (
                cart.getItems() == null ||
                        cart.getItems().isEmpty()
        ) {

            throw new RuntimeException(
                    "Cart is empty"
            );
        }


        Order order =
                new Order();

        order.setUser(user);

        order.setFirstName(
                request.getFirstName()
        );

        order.setLastName(
                request.getLastName()
        );

        order.setPhone(
                request.getPhone()
        );

        order.setAlternatePhone(
                request.getAlternatePhone()
        );

        order.setAddress(
                request.getAddress()
        );

        order.setCity(
                request.getCity()
        );

        order.setProvince(
                request.getProvince()
        );

        order.setDeliveryNotes(
                request.getDeliveryNotes()
        );

        order.setPaymentMethod(
                request.getPaymentMethod()
        );


        double subtotal = 0;


        List<OrderItem> items =
                new ArrayList<>();


        for (
                CartItem cartItem :
                cart.getItems()
        ) {

            Product product =
                    cartItem.getProduct();


            if (
                    product.getStock()
                            <
                            cartItem.getQuantity()
            ) {

                throw new RuntimeException(
                        "Not enough stock for " +
                                product.getName()
                );
            }


            double price =
                    cartItem.getPrice();


            double itemSubtotal =
                    price *
                            cartItem.getQuantity();


            OrderItem item =
                    new OrderItem();

            item.setOrder(order);

            item.setProduct(product);

            item.setQuantity(
                    cartItem.getQuantity()
            );

            item.setPrice(price);

            item.setSubtotal(
                    itemSubtotal
            );


            items.add(item);


            subtotal +=
                    itemSubtotal;


            product.setStock(
                    product.getStock()
                            -
                            cartItem.getQuantity()
            );

            productRepository.save(
                    product
            );
        }


        double deliveryFee =
                "EXPRESS".equalsIgnoreCase(
                        request.getDeliveryMethod()
                )
                        ? 150
                        : 0;


        double total =
                subtotal +
                        deliveryFee;


        order.setSubtotal(
                subtotal
        );

        order.setDeliveryFee(
                deliveryFee
        );

        order.setTotalAmount(
                total
        );

        order.setItems(
                items
        );


        Order savedOrder =
                orderRepository.save(
                        order
                );


        /*
         * Payment
         */

        Payment payment =
                new Payment();

        payment.setOrder(
                savedOrder
        );

        payment.setMethod(
                request.getPaymentMethod()
        );

        payment.setAmount(
                total
        );

        payment.setStatus(
                "COD".equalsIgnoreCase(
                        request.getPaymentMethod()
                )
                        ? "PENDING"
                        : "PENDING"
        );

        paymentRepository.save(
                payment
        );


        /*
         * Delivery
         */

        Delivery delivery =
                new Delivery();

        delivery.setOrder(
                savedOrder
        );

        delivery.setMethod(
                request.getDeliveryMethod()
        );

        delivery.setStatus(
                "PREPARING"
        );

        delivery.setTrackingNumber(
                "GLW-" +
                        System.currentTimeMillis()
        );

        deliveryRepository.save(
                delivery
        );


        /*
         * Empty cart
         */

        cart.getItems().clear();

        cartRepository.save(
                cart
        );


        /*
         * Send confirmation email
         */

        try {

            emailService.sendOrderConfirmation(
                    user.getEmail(),
                    savedOrder
            );

        } catch (Exception e) {

            System.err.println(
                    "Email failed: " +
                            e.getMessage()
            );
        }


        return savedOrder;
    }


    public List<Order> getUserOrders(
            String email
    ) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );

        return orderRepository
                .findByUserOrderByCreatedAtDesc(
                        user
                );
    }
}