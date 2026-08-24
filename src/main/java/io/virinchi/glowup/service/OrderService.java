package io.virinchi.glowup.service;

import io.virinchi.glowup.dto.CreateOrderRequest;
import io.virinchi.glowup.dto.OrderItemRequest;
import io.virinchi.glowup.entity.*;
import io.virinchi.glowup.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

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
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.deliveryRepository = deliveryRepository;
        this.emailService = emailService;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        if (request == null) {
            throw new RuntimeException("Order request cannot be empty");
        }

        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        User user = null;
        if (!email.isEmpty()) {
            user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                // Auto-create customer profile
                user = new User();
                user.setEmail(email);
                user.setFullName((request.getFirstName() != null ? request.getFirstName() : "") + " " + (request.getLastName() != null ? request.getLastName() : "").trim());
                user.setPhoneNumber(request.getPhone() != null && !request.getPhone().trim().isEmpty() ? request.getPhone().trim() : "9800000000");
                user.setPassword("guest_order_session");
                user.setRole("CUSTOMER");
                user.setVerified(true);
                user = userRepository.save(user);
            }
        }

        Order order = new Order();
        order.setUser(user);
        order.setFirstName(request.getFirstName() != null && !request.getFirstName().trim().isEmpty() ? request.getFirstName().trim() : "Customer");
        order.setLastName(request.getLastName() != null ? request.getLastName().trim() : "");
        order.setPhone(request.getPhone() != null ? request.getPhone().trim() : "");
        order.setAlternatePhone(request.getAlternatePhone());
        order.setAddress(request.getAddress() != null ? request.getAddress().trim() : "Kathmandu");
        order.setCity(request.getCity() != null ? request.getCity().trim() : "Kathmandu");
        order.setProvince(request.getProvince() != null ? request.getProvince().trim() : "Bagmati Province");
        order.setDeliveryNotes(request.getDeliveryNotes());

        String paymentMethod = request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "COD";
        order.setPaymentMethod(paymentMethod);

        boolean isPaid = "PAID".equalsIgnoreCase(request.getPaymentStatus()) ||
                "ESEWA".equalsIgnoreCase(paymentMethod) ||
                "KHALTI".equalsIgnoreCase(paymentMethod) ||
                "FONEPAY".equalsIgnoreCase(paymentMethod);
        order.setPaymentStatus(isPaid ? "PAID" : "PENDING");
        order.setOrderStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        double subtotal = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        // 1. Process items directly provided in CreateOrderRequest (from frontend checkout)
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (OrderItemRequest itemReq : request.getItems()) {
                String pName = itemReq.getProductName() != null ? itemReq.getProductName().trim() : "Product";
                Product product = null;

                if (itemReq.getProductId() != null) {
                    product = productRepository.findById(itemReq.getProductId()).orElse(null);
                }
                if (product == null) {
                    product = productRepository.findByNameIgnoreCase(pName).orElse(null);
                }
                if (product == null) {
                    product = new Product();
                    product.setName(pName);
                    product.setPrice(itemReq.getPrice() > 0 ? itemReq.getPrice() : 1000.0);
                    product.setStock(100);
                    product.setRating(5.0);
                    product.setImage(itemReq.getImage() != null ? itemReq.getImage() : "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&q=80");
                    product = productRepository.save(product);
                }

                int qty = itemReq.getQuantity() > 0 ? itemReq.getQuantity() : 1;
                double itemPrice = itemReq.getPrice() > 0 ? itemReq.getPrice() : product.getPrice();
                double lineTotal = itemPrice * qty;

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(qty);
                orderItem.setPrice(itemPrice);
                orderItem.setSubtotal(lineTotal);
                orderItems.add(orderItem);

                subtotal += lineTotal;

                if (product.getStock() >= qty) {
                    product.setStock(product.getStock() - qty);
                    productRepository.save(product);
                }
            }
        }
        // 2. Fallback: check if user has items in DB cart
        else if (user != null) {
            Cart cart = cartRepository.findByUser(user).orElse(null);
            if (cart != null && cart.getItems() != null && !cart.getItems().isEmpty()) {
                for (CartItem cartItem : cart.getItems()) {
                    Product product = cartItem.getProduct();
                    double price = cartItem.getPrice() > 0 ? cartItem.getPrice() : product.getPrice();
                    double lineTotal = price * cartItem.getQuantity();

                    OrderItem item = new OrderItem();
                    item.setOrder(order);
                    item.setProduct(product);
                    item.setQuantity(cartItem.getQuantity());
                    item.setPrice(price);
                    item.setSubtotal(lineTotal);
                    orderItems.add(item);

                    subtotal += lineTotal;

                    if (product.getStock() >= cartItem.getQuantity()) {
                        product.setStock(product.getStock() - cartItem.getQuantity());
                        productRepository.save(product);
                    }
                }
                cart.getItems().clear();
                cartRepository.save(cart);
            }
        }

        // If subtotal is specified in request, verify
        if (request.getSubtotal() != null && request.getSubtotal() > 0) {
            subtotal = request.getSubtotal();
        }

        double deliveryFee = "EXPRESS".equalsIgnoreCase(request.getDeliveryMethod()) ? 150.0 : 0.0;
        if (request.getDeliveryFee() != null) {
            deliveryFee = request.getDeliveryFee();
        }

        double total = request.getTotalAmount() != null && request.getTotalAmount() > 0
                ? request.getTotalAmount()
                : (subtotal + deliveryFee);

        order.setSubtotal(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setTotalAmount(total);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        log.info("Order saved in DB: ID={}, Total={}", savedOrder.getId(), savedOrder.getTotalAmount());

        // Create and save Payment entity
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setMethod(savedOrder.getPaymentMethod());
        payment.setAmount(total);
        payment.setStatus(savedOrder.getPaymentStatus());
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Create and save Delivery entity
        Delivery delivery = new Delivery();
        delivery.setOrder(savedOrder);
        delivery.setMethod(request.getDeliveryMethod() != null ? request.getDeliveryMethod().toUpperCase() : "STANDARD");
        delivery.setStatus("PREPARING");
        delivery.setTrackingNumber("GLW-" + savedOrder.getId() + "-" + (System.currentTimeMillis() % 100000));
        delivery.setCreatedAt(LocalDateTime.now());
        deliveryRepository.save(delivery);

        // 1. Send Order Confirmation Email
        String recipientEmail = !email.isEmpty() ? email : "rajmaharjan738@gmail.com";
        try {
            emailService.sendOrderConfirmation(recipientEmail, savedOrder);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email: {}", e.getMessage());
        }

        // 2. If Paid (eSewa / Khalti / Fonepay), send Payment Confirmation Email
        if ("PAID".equalsIgnoreCase(savedOrder.getPaymentStatus())) {
            try {
                emailService.sendPaymentConfirmation(recipientEmail, savedOrder, payment);
            } catch (Exception e) {
                log.error("Failed to send payment confirmation email: {}", e.getMessage());
            }
        }

        return savedOrder;
    }

    public List<Order> getUserOrders(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return new ArrayList<>();
        }
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        String oldStatus = order.getOrderStatus();
        String normalizedStatus = newStatus != null ? newStatus.toUpperCase() : "PENDING";
        order.setOrderStatus(normalizedStatus);

        Delivery delivery = deliveryRepository.findAll().stream()
                .filter(d -> d.getOrder() != null && d.getOrder().getId().equals(order.getId()))
                .findFirst().orElse(null);

        if (delivery != null) {
            if ("SHIPPED".equalsIgnoreCase(normalizedStatus) || "OUT_FOR_DELIVERY".equalsIgnoreCase(normalizedStatus)) {
                delivery.setStatus("OUT_FOR_DELIVERY");
                deliveryRepository.save(delivery);
            } else if ("DELIVERED".equalsIgnoreCase(normalizedStatus)) {
                delivery.setStatus("DELIVERED");
                deliveryRepository.save(delivery);
            } else if ("CANCELLED".equalsIgnoreCase(normalizedStatus)) {
                delivery.setStatus("CANCELLED");
                deliveryRepository.save(delivery);
            }
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order #{} status updated from {} to {}", orderId, oldStatus, normalizedStatus);

        String recipientEmail = (order.getUser() != null && order.getUser().getEmail() != null)
                ? order.getUser().getEmail()
                : "rajmaharjan738@gmail.com";

        // Trigger corresponding notification email based on status transition
        try {
            if ("SHIPPED".equalsIgnoreCase(normalizedStatus) || "OUT_FOR_DELIVERY".equalsIgnoreCase(normalizedStatus)) {
                emailService.sendOrderShippedNotification(recipientEmail, updatedOrder, delivery);
            } else if ("DELIVERED".equalsIgnoreCase(normalizedStatus)) {
                emailService.sendOrderDeliveredNotification(recipientEmail, updatedOrder);
            } else if ("CANCELLED".equalsIgnoreCase(normalizedStatus)) {
                emailService.sendOrderCancelledNotification(recipientEmail, updatedOrder, reason);
            }
        } catch (Exception e) {
            log.error("Failed to send status update email for Order #{}: {}", orderId, e.getMessage());
        }

        return updatedOrder;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) {
        return updateOrderStatus(orderId, newStatus, null);
    }
}