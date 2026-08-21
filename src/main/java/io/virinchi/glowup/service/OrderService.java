package io.virinchi.glowup.service;

import io.virinchi.glowup.dto.CreateOrderRequest;
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
        }

        Order order = new Order();
        order.setUser(user);
        order.setFirstName(request.getFirstName() != null ? request.getFirstName() : "Customer");
        order.setLastName(request.getLastName() != null ? request.getLastName() : "");
        order.setPhone(request.getPhone() != null ? request.getPhone() : "");
        order.setAlternatePhone(request.getAlternatePhone());
        order.setAddress(request.getAddress() != null ? request.getAddress() : "Kathmandu");
        order.setCity(request.getCity() != null ? request.getCity() : "Kathmandu");
        order.setProvince(request.getProvince() != null ? request.getProvince() : "Bagmati");
        order.setDeliveryNotes(request.getDeliveryNotes());
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "COD");
        order.setPaymentStatus("COD".equalsIgnoreCase(request.getPaymentMethod()) ? "PENDING" : "PAID");
        order.setOrderStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        double subtotal = 0.0;
        List<OrderItem> items = new ArrayList<>();

        // Check if user has items in DB cart
        Cart cart = user != null ? cartRepository.findByUser(user).orElse(null) : null;

        if (cart != null && cart.getItems() != null && !cart.getItems().isEmpty()) {
            for (CartItem cartItem : cart.getItems()) {
                Product product = cartItem.getProduct();
                double price = cartItem.getPrice() > 0 ? cartItem.getPrice() : product.getPrice();
                double itemSubtotal = price * cartItem.getQuantity();

                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setProduct(product);
                item.setQuantity(cartItem.getQuantity());
                item.setPrice(price);
                item.setSubtotal(itemSubtotal);
                items.add(item);

                subtotal += itemSubtotal;

                // Reduce stock safely
                if (product.getStock() >= cartItem.getQuantity()) {
                    product.setStock(product.getStock() - cartItem.getQuantity());
                    productRepository.save(product);
                }
            }

            // Clear the DB cart
            cart.getItems().clear();
            cartRepository.save(cart);
        } else {
            // Default placeholder product if direct checkout without DB cart
            Product defaultProd = productRepository.findAll().stream().findFirst().orElse(null);
            if (defaultProd != null) {
                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setProduct(defaultProd);
                item.setQuantity(1);
                item.setPrice(defaultProd.getPrice());
                item.setSubtotal(defaultProd.getPrice());
                items.add(item);
                subtotal += defaultProd.getPrice();
            }
        }

        double deliveryFee = "EXPRESS".equalsIgnoreCase(request.getDeliveryMethod()) ? 150.0 : 0.0;
        double total = subtotal + deliveryFee;

        order.setSubtotal(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setTotalAmount(total);
        order.setItems(items);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: ID={}, Total={}", savedOrder.getId(), savedOrder.getTotalAmount());

        // Create Payment record
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setMethod(savedOrder.getPaymentMethod());
        payment.setAmount(total);
        payment.setStatus(savedOrder.getPaymentStatus());
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Create Delivery record
        Delivery delivery = new Delivery();
        delivery.setOrder(savedOrder);
        delivery.setMethod(request.getDeliveryMethod() != null ? request.getDeliveryMethod() : "STANDARD");
        delivery.setStatus("PREPARING");
        delivery.setTrackingNumber("GLW-" + savedOrder.getId() + "-" + System.currentTimeMillis() % 100000);
        delivery.setCreatedAt(LocalDateTime.now());
        deliveryRepository.save(delivery);

        // Send order confirmation email notification
        try {
            String recipientEmail = !email.isEmpty() ? email : "rajmaharjan738@gmail.com";
            emailService.sendOrderConfirmation(recipientEmail, savedOrder);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email: {}", e.getMessage());
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
    public Order updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        order.setOrderStatus(newStatus);
        return orderRepository.save(order);
    }
}