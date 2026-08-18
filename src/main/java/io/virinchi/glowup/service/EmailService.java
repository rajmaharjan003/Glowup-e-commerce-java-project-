package io.virinchi.glowup.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmation(
            String customerEmail,
            String customerName,
            Long orderId,
            double amount) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("yourglowupemail@gmail.com");
        message.setTo(customerEmail);

        message.setSubject(
                "GlowUp Nepal - Order Confirmation #" + orderId
        );

        String emailBody =
                "Hello " + customerName + ",\n\n" +

                        "Thank you for shopping with GlowUp Nepal!\n\n" +

                        "Your order has been successfully placed.\n\n" +

                        "Order ID: #" + orderId + "\n" +
                        "Total Amount: Rs. " + amount + "\n" +
                        "Order Status: CONFIRMED\n\n" +

                        "We will notify you when your order is shipped.\n\n" +

                        "Thank you for choosing GlowUp Nepal!\n\n" +
                        "Regards,\n" +
                        "GlowUp Nepal Team";

        message.setText(emailBody);

        mailSender.send(message);
    }
}