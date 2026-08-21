package io.virinchi.glowup.service;

import io.virinchi.glowup.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ==========================================
    // WELCOME EMAIL ON SIGNUP
    // ==========================================
    public void sendWelcomeEmail(String to, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Welcome to GlowUp Nepal – Your Account is Ready! 🎉");
            
            String body = "Dear " + (name != null ? name : "Customer") + ",\n\n" +
                    "Welcome to GlowUp Nepal – your trusted destination for premium cosmetics, electronics, and lifestyle products!\n\n" +
                    "Your account has been successfully created.\n\n" +
                    "Here's what you can do now:\n" +
                    "• Explore thousands of genuine products with nationwide delivery across Nepal\n" +
                    "• Enjoy fast checkout with eSewa, Khalti, Fonepay QR, and Cash on Delivery\n" +
                    "• Track your orders live in real-time\n\n" +
                    "Visit our store: http://localhost:8080 or open your GlowUp store app.\n\n" +
                    "Best regards,\n" +
                    "The GlowUp Nepal Team\n" +
                    "support@glowup.com";

            message.setText(body);
            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", to, e.getMessage());
        }
    }

    // ==========================================
    // ORDER CONFIRMATION EMAIL
    // ==========================================
    public void sendOrderConfirmation(String to, Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("GlowUp Nepal - Order #" + order.getId() + " Confirmed! 🛍️");

            String body = "Hello " + order.getFirstName() + ",\n\n" +
                    "Thank you for shopping with GlowUp Nepal!\n\n" +
                    "Your order has been successfully placed.\n\n" +
                    "----------------------------------------\n" +
                    "Order ID: #" + order.getId() + "\n" +
                    "Subtotal: Rs. " + order.getSubtotal() + "\n" +
                    "Delivery Fee: Rs. " + order.getDeliveryFee() + "\n" +
                    "Total Amount: Rs. " + order.getTotalAmount() + "\n" +
                    "Payment Method: " + order.getPaymentMethod() + " (" + order.getPaymentStatus() + ")\n" +
                    "Delivery Address: " + order.getAddress() + ", " + order.getCity() + (order.getProvince() != null ? ", " + order.getProvince() : "") + "\n" +
                    "Order Status: " + order.getOrderStatus() + "\n" +
                    "----------------------------------------\n\n" +
                    "We will notify you once your order is out for delivery.\n\n" +
                    "Track your order at any time using your Order ID #" + order.getId() + " on our Order Tracking page.\n\n" +
                    "Warm regards,\n" +
                    "GlowUp Nepal Customer Care";

            message.setText(body);
            mailSender.send(message);
            log.info("Order confirmation email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to {}: {}", to, e.getMessage());
        }
    }

    // ==========================================
    // REVIEW NOTIFICATION EMAIL
    // ==========================================
    public void sendReviewNotification(String to, String reviewerName, String productName, int rating, String comment) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("New Product Review Received: " + productName + " (" + rating + "★) 🌟");

            String body = "Hello,\n\n" +
                    "A new customer review has been submitted on GlowUp Nepal!\n\n" +
                    "----------------------------------------\n" +
                    "Product: " + productName + "\n" +
                    "Reviewer: " + reviewerName + "\n" +
                    "Rating: " + rating + " / 5 Stars (" + "★".repeat(rating) + ")\n" +
                    "Comment: \"" + comment + "\"\n" +
                    "----------------------------------------\n\n" +
                    "Thank you for sharing your valuable feedback with GlowUp Nepal.\n\n" +
                    "GlowUp Nepal Quality Team";

            message.setText(body);
            mailSender.send(message);
            log.info("Review notification email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send review notification email to {}: {}", to, e.getMessage());
        }
    }

    // ==========================================
    // TEST EMAIL
    // ==========================================
    public void sendTestEmail(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("GlowUp Nepal Email System Test");
        message.setText("Congratulations! Your GlowUp email configuration (SMTP Gmail) is working successfully.");
        mailSender.send(message);
    }
}