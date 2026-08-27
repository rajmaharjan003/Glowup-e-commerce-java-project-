package io.virinchi.glowup.service;

import io.virinchi.glowup.entity.Delivery;
import io.virinchi.glowup.entity.Order;
import io.virinchi.glowup.entity.OrderItem;
import io.virinchi.glowup.entity.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("rajmaharjan738@gmail.com");
            mailSender.send(message);
            log.info("Email sent successfully to: {} | Subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    // ==========================================
    // 1. REGISTRATION CONFIRMATION EMAIL
    // ==========================================
    public void sendWelcomeEmail(String to, String name) {
        String displayName = (name != null && !name.trim().isEmpty()) ? name : "Valued Customer";
        String subject = "Welcome to GlowUp Nepal – Your Account is Ready! 🎉";

        String body = "Dear " + displayName + ",\n\n" +
                "Welcome to GlowUp Nepal – your trusted destination for premium cosmetics, electronics, and lifestyle fashion!\n\n" +
                "Your account has been successfully created and verified.\n\n" +
                "Here is what you can do now:\n" +
                "• Browse thousands of genuine products across Nepal\n" +
                "• Enjoy fast express shipping to all 7 provinces\n" +
                "• Secure digital payments via eSewa, Khalti, Fonepay QR, and Cash on Delivery\n" +
                "• Live real-time order tracking and 7-day hassle-free returns\n\n" +
                "Visit our store: http://localhost:8080 or open your GlowUp store app.\n\n" +
                "Best regards,\n" +
                "The GlowUp Nepal Team\n" +
                "support@glowup.com | +977-9801234567";

        sendEmail(to, subject, body);
    }

    // ==========================================
    // 2. ORDER CONFIRMATION EMAIL
    // ==========================================
    public void sendOrderConfirmation(String to, Order order) {
        String subject = "GlowUp Nepal - Order #" + order.getId() + " Confirmed! 🛍️";
        String customerName = order.getFirstName() + (order.getLastName() != null ? " " + order.getLastName() : "");

        StringBuilder itemsText = new StringBuilder();
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                String prodName = item.getProduct() != null ? item.getProduct().getName() : "Product";
                itemsText.append("• ").append(prodName)
                        .append(" × ").append(item.getQuantity())
                        .append(" - Rs. ").append(df.format(item.getSubtotal()))
                        .append("\n");
            }
        } else {
            itemsText.append("• Standard Catalog Order Items\n");
        }

        String body = "Hello " + customerName + ",\n\n" +
                "Thank you for shopping with GlowUp Nepal!\n" +
                "Your order has been successfully placed and confirmed.\n\n" +
                "----------------------------------------\n" +
                "ORDER DETAILS\n" +
                "----------------------------------------\n" +
                "Order ID: #" + order.getId() + "\n" +
                "Delivery Address: " + order.getAddress() + ", " + order.getCity() + (order.getProvince() != null ? ", " + order.getProvince() : "") + "\n" +
                "Payment Method: " + order.getPaymentMethod() + " (" + order.getPaymentStatus() + ")\n" +
                "Subtotal: Rs. " + df.format(order.getSubtotal()) + "\n" +
                "Delivery Fee: Rs. " + df.format(order.getDeliveryFee()) + "\n" +
                "Total Amount: Rs. " + df.format(order.getTotalAmount()) + "\n\n" +
                "ORDER ITEMS:\n" +
                itemsText.toString() + "\n" +
                "----------------------------------------\n\n" +
                "We will notify you once your order is dispatched for delivery.\n" +
                "Track your order live anytime at: http://localhost:8080/order-tracking.html?id=" + order.getId() + "\n\n" +
                "Warm regards,\n" +
                "GlowUp Nepal Customer Care";

        sendEmail(to, subject, body);
    }

    // ==========================================
    // 3. PAYMENT CONFIRMATION EMAIL
    // ==========================================
    public void sendPaymentConfirmation(String to, Order order, Payment payment) {
        String subject = "Payment Confirmed for Order #" + order.getId() + " - GlowUp Nepal 💳";
        String customerName = order.getFirstName() + (order.getLastName() != null ? " " + order.getLastName() : "");
        double amount = payment != null ? payment.getAmount() : order.getTotalAmount();
        String method = payment != null ? payment.getMethod() : order.getPaymentMethod();

        String body = "Dear " + customerName + ",\n\n" +
                "We have received and successfully verified your payment for Order #" + order.getId() + ".\n\n" +
                "----------------------------------------\n" +
                "PAYMENT RECEIPT\n" +
                "----------------------------------------\n" +
                "Order ID: #" + order.getId() + "\n" +
                "Amount Paid: Rs. " + df.format(amount) + "\n" +
                "Payment Method: " + method + "\n" +
                "Payment Status: VERIFIED & COMPLETED\n" +
                "Transaction Date: " + java.time.LocalDateTime.now() + "\n" +
                "----------------------------------------\n\n" +
                "Thank you for choosing digital checkout with GlowUp Nepal.\n\n" +
                "Track order status: http://localhost:8080/order-tracking.html?id=" + order.getId() + "\n\n" +
                "Best regards,\n" +
                "GlowUp Nepal Accounts & Finance Desk";

        sendEmail(to, subject, body);
    }

    // ==========================================
    // 4. ORDER SHIPPED NOTIFICATION
    // ==========================================
    public void sendOrderShippedNotification(String to, Order order, Delivery delivery) {
        String subject = "Your GlowUp Order #" + order.getId() + " Has Shipped! 🚚⚡";
        String customerName = order.getFirstName() + (order.getLastName() != null ? " " + order.getLastName() : "");
        String trackingCode = delivery != null && delivery.getTrackingNumber() != null ? delivery.getTrackingNumber() : ("GLW-" + order.getId() + "-NEPAL");

        String body = "Hello " + customerName + ",\n\n" +
                "Great news! Your GlowUp Order #" + order.getId() + " is now on its way to you!\n\n" +
                "----------------------------------------\n" +
                "SHIPPING DETAILS\n" +
                "----------------------------------------\n" +
                "Tracking Number: " + trackingCode + "\n" +
                "Courier Service: GlowUp Express Courier Nepal\n" +
                "Destination: " + order.getAddress() + ", " + order.getCity() + "\n" +
                "Recipient Contact: " + order.getPhone() + "\n" +
                "Estimated Delivery: Today / Tomorrow\n" +
                "----------------------------------------\n\n" +
                "Please make sure your phone is active and reachable for our delivery rider.\n" +
                "Track live route: http://localhost:8080/order-tracking.html?id=" + order.getId() + "\n\n" +
                "Warm regards,\n" +
                "GlowUp Nepal Logistics Team";

        sendEmail(to, subject, body);
    }

    // ==========================================
    // 5. ORDER DELIVERED NOTIFICATION
    // ==========================================
    public void sendOrderDeliveredNotification(String to, Order order) {
        String subject = "Package Delivered! Order #" + order.getId() + " - GlowUp Nepal 🎁";
        String customerName = order.getFirstName() + (order.getLastName() != null ? " " + order.getLastName() : "");

        String body = "Dear " + customerName + ",\n\n" +
                "Your Order #" + order.getId() + " has been successfully delivered!\n\n" +
                "We hope you enjoy your new items. If you have a moment, please share your product review and rating:\n" +
                "http://localhost:8080/order-tracking.html?id=" + order.getId() + "\n\n" +
                "If you need any return or exchange assistance, contact us within 7 days at support@glowup.com.\n\n" +
                "Thank you for shopping with GlowUp Nepal!\n" +
                "GlowUp Nepal Customer Happiness Team";

        sendEmail(to, subject, body);
    }

    // ==========================================
    // 6. ORDER CANCELLATION NOTIFICATION
    // ==========================================
    public void sendOrderCancelledNotification(String to, Order order, String reason) {
        String subject = "Order Cancellation Notice: #" + order.getId() + " - GlowUp Nepal 🛑";
        String customerName = order.getFirstName() + (order.getLastName() != null ? " " + order.getLastName() : "");
        String cancelReason = (reason != null && !reason.trim().isEmpty()) ? reason : "Customer request / Order modification";

        String body = "Dear " + customerName + ",\n\n" +
                "This email confirms that your GlowUp Order #" + order.getId() + " has been cancelled.\n\n" +
                "----------------------------------------\n" +
                "CANCELLATION DETAILS\n" +
                "----------------------------------------\n" +
                "Order ID: #" + order.getId() + "\n" +
                "Reason: " + cancelReason + "\n" +
                "Total Amount: Rs. " + df.format(order.getTotalAmount()) + "\n" +
                "Payment Status: " + ("PAID".equalsIgnoreCase(order.getPaymentStatus()) ? "Online Refund Initiated (24-48h)" : "No Charge (COD)") + "\n" +
                "----------------------------------------\n\n" +
                "If you made an online prepayment (eSewa / Khalti / Fonepay), the refund will be credited back to your account within 24-48 banking hours.\n\n" +
                "Browse more products anytime: http://localhost:8080/index.html\n\n" +
                "Best regards,\n" +
                "GlowUp Nepal Customer Care";

        sendEmail(to, subject, body);
    }

    // ==========================================
    // 7. FORGOT PASSWORD OTP VERIFICATION EMAIL
    // ==========================================
    public void sendPasswordResetOtpEmail(String to, String name, String otp) {
        String subject = "Your GlowUp Password Reset Verification Code: " + otp + " 🔐";
        String displayName = (name != null && !name.trim().isEmpty()) ? name : "User";

        String body = "Hello " + displayName + ",\n\n" +
                "We received a request to reset your GlowUp account password.\n\n" +
                "Your 4-digit verification code is:\n\n" +
                "        ▶   " + otp + "   ◀\n\n" +
                "This verification code is valid for 10 minutes.\n\n" +
                "SECURITY WARNING:\n" +
                "Never share this OTP code with anyone. GlowUp staff will never ask for your code.\n" +
                "If you did not request a password reset, please disregard this message.\n\n" +
                "Best regards,\n" +
                "GlowUp Nepal Security Desk";

        sendEmail(to, subject, body);
    }

    // ==========================================
    // 8. PASSWORD CHANGED CONFIRMATION EMAIL
    // ==========================================
    public void sendPasswordChangedNotification(String to, String name) {
        String subject = "Your GlowUp Password Has Been Changed Successfully 🔒";
        String displayName = (name != null && !name.trim().isEmpty()) ? name : "User";

        String body = "Dear " + displayName + ",\n\n" +
                "The password for your GlowUp account (" + to + ") has been successfully changed.\n\n" +
                "If you performed this action, you can safely ignore this email.\n" +
                "If you did not make this change, please reset your password immediately or contact our security desk at support@glowup.com.\n\n" +
                "GlowUp Nepal Security Desk";

        sendEmail(to, subject, body);
    }

    // ==========================================
    // 9. REVIEW NOTIFICATIONS (ADMIN & CUSTOMER)
    // ==========================================
    public void sendAdminReviewNotification(String to, String reviewerName, String reviewerEmail, String productName, int rating, String comment) {
        String subject = "🔔 [Admin Alert] New Review on " + productName + " (" + rating + "★)";
        String body = "Dear Administrator,\n\n" +
                "A customer has just submitted a new product review on GlowUp Nepal!\n\n" +
                "----------------------------------------\n" +
                "REVIEW DETAILS\n" +
                "----------------------------------------\n" +
                "Product: " + productName + "\n" +
                "Reviewer Name: " + (reviewerName != null ? reviewerName : "Customer") + "\n" +
                "Reviewer Email: " + (reviewerEmail != null && !reviewerEmail.isEmpty() ? reviewerEmail : "N/A") + "\n" +
                "Rating: " + rating + " / 5 Stars (" + "★".repeat(rating) + ")\n" +
                "Review / Comment:\n" +
                "\"" + comment + "\"\n" +
                "Submitted At: " + java.time.LocalDateTime.now() + "\n" +
                "----------------------------------------\n\n" +
                "You can manage all reviews in your Admin Dashboard: http://localhost:8080/admin.html\n\n" +
                "GlowUp Nepal Automated Notification System";

        sendEmail(to, subject, body);
    }

    public void sendCustomerReviewConfirmation(String to, String reviewerName, String productName, int rating) {
        String subject = "Thank you for reviewing " + productName + " on GlowUp Nepal! 🌟";
        String displayName = (reviewerName != null && !reviewerName.trim().isEmpty()) ? reviewerName : "Valued Customer";

        String body = "Dear " + displayName + ",\n\n" +
                "Thank you for taking the time to share your feedback on " + productName + " (" + rating + "★)!\n\n" +
                "Your review helps thousands of shoppers in Nepal make confident purchase decisions.\n" +
                "Our store administrator has been notified of your review.\n\n" +
                "Keep exploring more exciting deals: http://localhost:8080/index.html\n\n" +
                "Warm regards,\n" +
                "GlowUp Nepal Community Team";

        sendEmail(to, subject, body);
    }

    public void sendReviewNotification(String to, String reviewerName, String productName, int rating, String comment) {
        sendAdminReviewNotification(to, reviewerName, "", productName, rating, comment);
    }

    // ==========================================
    // 10. SYSTEM TEST EMAIL
    // ==========================================
    public void sendTestEmail(String to) {
        sendWelcomeEmail(to, "Test User");
    }
}