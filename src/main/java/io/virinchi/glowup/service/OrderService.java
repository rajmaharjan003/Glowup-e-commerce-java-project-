package io.virinchi.glowup.service;

import io.virinchi.glowup.dto.CreateOrderRequest;
import io.virinchi.glowup.dto.OrderItemRequest;
import io.virinchi.glowup.entity.*;
import io.virinchi.glowup.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private final PasswordEncoder passwordEncoder;

    public OrderService(
            UserRepository userRepository,
            CartRepository cartRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            DeliveryRepository deliveryRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.deliveryRepository = deliveryRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        if (request == null) {
            throw new RuntimeException("Order request cannot be empty");
        }

        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        User user = null;
        if (!email.isEmpty()) {
            user = userRepository.findByEmailIgnoreCase(email).orElse(null);
            if (user == null) {
                // Auto-create customer profile for guest checkout safely
                try {
                    User newUser = new User();
                    newUser.setEmail(email);
                    String fName = request.getFirstName() != null ? request.getFirstName().trim() : "";
                    String lName = request.getLastName() != null ? request.getLastName().trim() : "";
                    newUser.setFullName((fName + " " + lName).trim().isEmpty() ? email.split("@")[0] : (fName + " " + lName).trim());
                    
                    String reqPhone = request.getPhone() != null && !request.getPhone().trim().isEmpty() ? request.getPhone().trim() : null;
                    if (reqPhone != null && userRepository.findByPhoneNumber(reqPhone).isEmpty()) {
                        newUser.setPhoneNumber(reqPhone);
                    }
                    
                    newUser.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                    newUser.setAuthProvider("GUEST");
                    newUser.setRole("CUSTOMER");
                    newUser.setVerified(true);
                    user = userRepository.save(newUser);
                    log.info("Created guest user record for email: {}", email);
                } catch (Exception e) {
                    log.warn("Notice creating guest user account: {}, continuing order placement", e.getMessage());
                }
            }
        }

        Order order = new Order();
        order.setUser(user);
        order.setEmail(!email.isEmpty() ? email : (user != null ? user.getEmail() : "customer@glowup.com"));
        order.setFirstName(request.getFirstName() != null && !request.getFirstName().trim().isEmpty() ? request.getFirstName().trim() : "Customer");
        order.setLastName(request.getLastName() != null ? request.getLastName().trim() : "");
        order.setPhone(request.getPhone() != null && !request.getPhone().trim().isEmpty() ? request.getPhone().trim() : "9800000000");
        order.setAlternatePhone(request.getAlternatePhone());
        order.setAddress(request.getAddress() != null && !request.getAddress().trim().isEmpty() ? request.getAddress().trim() : "Kathmandu");
        order.setCity(request.getCity() != null && !request.getCity().trim().isEmpty() ? request.getCity().trim() : "Kathmandu");
        order.setProvince(request.getProvince() != null && !request.getProvince().trim().isEmpty() ? request.getProvince().trim() : "Bagmati Province");
        order.setDeliveryNotes(request.getDeliveryNotes());
        order.setDeliveryMethod(request.getDeliveryMethod() != null ? request.getDeliveryMethod().toUpperCase() : "STANDARD");

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
                    product = productRepository.findFirstByNameIgnoreCase(pName).orElse(null);
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
        log.info("Order saved successfully in DB: ID={}, Email={}, Total={}", savedOrder.getId(), savedOrder.getEmail(), savedOrder.getTotalAmount());

        // Create and save Payment entity
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setMethod(savedOrder.getPaymentMethod());
        payment.setAmount(total);
        payment.setStatus(savedOrder.getPaymentStatus());
        payment.setTransactionId("TXN-" + System.currentTimeMillis() + "-" + savedOrder.getId());
        payment.setCreatedAt(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        // Create and save Delivery entity
        Delivery delivery = new Delivery();
        delivery.setOrder(savedOrder);
        delivery.setMethod(request.getDeliveryMethod() != null ? request.getDeliveryMethod().toUpperCase() : "STANDARD");
        delivery.setStatus("PREPARING");
        delivery.setTrackingNumber("GLW-" + savedOrder.getId() + "-" + (System.currentTimeMillis() % 100000));
        delivery.setEstimatedDate(LocalDateTime.now().plusDays("EXPRESS".equalsIgnoreCase(request.getDeliveryMethod()) ? 1 : 3));
        delivery.setCreatedAt(LocalDateTime.now());
        Delivery savedDelivery = deliveryRepository.save(delivery);

        // Send Email Confirmation Asynchronously so SMTP never blocks order response
        final Order finalOrder = savedOrder;
        final Payment finalPayment = savedPayment;
        final String finalRecipient = !email.isEmpty() ? email : "rajmaharjan738@gmail.com";

        new Thread(() -> {
            try {
                emailService.sendOrderConfirmation(finalRecipient, finalOrder);
                if ("PAID".equalsIgnoreCase(finalOrder.getPaymentStatus())) {
                    emailService.sendPaymentConfirmation(finalRecipient, finalOrder, finalPayment);
                }
            } catch (Exception e) {
                log.error("Async email dispatch failed for Order #{}: {}", finalOrder.getId(), e.getMessage());
            }
        }).start();

        return savedOrder;
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    public Order resolveOrder(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }
        String clean = identifier.trim();

        // 1. Try direct numeric parse
        try {
            Long numId = Long.parseLong(clean);
            Order o = orderRepository.findById(numId).orElse(null);
            if (o != null) return o;
        } catch (NumberFormatException ignored) {}

        // 2. Try stripping #, GU-, GLW- prefixes (e.g. #GU-1 -> 1, GU-1 -> 1, #1 -> 1)
        String stripped = clean.replaceAll("(?i)^#", "")
                .replaceAll("(?i)^gu-", "")
                .replaceAll("(?i)^glw-", "")
                .replaceAll("(?i)^\\d{4}-", "");
        try {
            Long numId = Long.parseLong(stripped);
            Order o = orderRepository.findById(numId).orElse(null);
            if (o != null) return o;
        } catch (NumberFormatException ignored) {}

        // 3. Try tracking number lookup
        Delivery delivery = deliveryRepository.findByTrackingNumber(clean).orElse(null);
        if (delivery != null && delivery.getOrder() != null) {
            return delivery.getOrder();
        }

        // 4. Try matching latest order by ID, tracking, phone or email
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(o -> ("GU-" + o.getId()).equalsIgnoreCase(clean) ||
                             ("#GU-" + o.getId()).equalsIgnoreCase(clean) ||
                             String.valueOf(o.getId()).equals(clean) ||
                             (o.getEmail() != null && o.getEmail().equalsIgnoreCase(clean)) ||
                             (o.getPhone() != null && o.getPhone().equals(clean)))
                .findFirst().orElse(null);
    }

    public List<Order> getUserOrders(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String cleanEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(cleanEmail).orElse(null);
        
        List<Order> orders = new ArrayList<>();
        if (user != null) {
            orders.addAll(orderRepository.findByUserOrderByCreatedAtDesc(user));
        }

        // Also include any orders placed directly with this email
        List<Order> emailOrders = orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(o -> cleanEmail.equalsIgnoreCase(o.getEmail()) || (o.getUser() != null && cleanEmail.equalsIgnoreCase(o.getUser().getEmail())))
                .filter(o -> !orders.contains(o))
                .toList();
        orders.addAll(emailOrders);

        return orders;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Delivery getDeliveryForOrder(Long orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId).orElse(null);
        if (delivery == null) {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                delivery = new Delivery();
                delivery.setOrder(order);
                delivery.setMethod("STANDARD");
                delivery.setStatus("PREPARING");
                delivery.setTrackingNumber("GLW-" + order.getId() + "-NEPAL");
                delivery.setEstimatedDate(LocalDateTime.now().plusDays(3));
                delivery.setCreatedAt(order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now());
                delivery = deliveryRepository.save(delivery);
            }
        }
        return delivery;
    }

    public Payment getPaymentForOrder(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (payment == null) {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                payment = new Payment();
                payment.setOrder(order);
                payment.setMethod(order.getPaymentMethod());
                payment.setAmount(order.getTotalAmount());
                payment.setStatus(order.getPaymentStatus());
                payment.setTransactionId("TXN-" + order.getId() + "-AUTO");
                payment.setCreatedAt(order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now());
                payment = paymentRepository.save(payment);
            }
        }
        return payment;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        String oldStatus = order.getOrderStatus();
        String normalizedStatus = newStatus != null ? newStatus.toUpperCase() : "PENDING";
        order.setOrderStatus(normalizedStatus);

        Delivery delivery = getDeliveryForOrder(order.getId());
        Payment payment = getPaymentForOrder(order.getId());

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
            } else if ("PROCESSING".equalsIgnoreCase(normalizedStatus) || "PACKED".equalsIgnoreCase(normalizedStatus)) {
                delivery.setStatus(normalizedStatus);
                deliveryRepository.save(delivery);
            }
        }

        // If DELIVERED, ensure payment status is marked as PAID (e.g. COD collected upon delivery)
        if ("DELIVERED".equalsIgnoreCase(normalizedStatus)) {
            order.setPaymentStatus("PAID");
            if (payment != null) {
                payment.setStatus("PAID");
                paymentRepository.save(payment);
            }
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order #{} status updated from {} to {}", orderId, oldStatus, normalizedStatus);

        String recipientEmail = (order.getEmail() != null && !order.getEmail().trim().isEmpty())
                ? order.getEmail()
                : (order.getUser() != null && order.getUser().getEmail() != null ? order.getUser().getEmail() : "rajmaharjan738@gmail.com");

        final Delivery finalDel = delivery;
        new Thread(() -> {
            try {
                if ("SHIPPED".equalsIgnoreCase(normalizedStatus) || "OUT_FOR_DELIVERY".equalsIgnoreCase(normalizedStatus)) {
                    emailService.sendOrderShippedNotification(recipientEmail, updatedOrder, finalDel);
                } else if ("DELIVERED".equalsIgnoreCase(normalizedStatus)) {
                    emailService.sendOrderDeliveredNotification(recipientEmail, updatedOrder);
                } else if ("CANCELLED".equalsIgnoreCase(normalizedStatus)) {
                    emailService.sendOrderCancelledNotification(recipientEmail, updatedOrder, reason);
                }
            } catch (Exception e) {
                log.error("Failed to send status update email for Order #{}: {}", orderId, e.getMessage());
            }
        }).start();

        return updatedOrder;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) {
        return updateOrderStatus(orderId, newStatus, null);
    }
}