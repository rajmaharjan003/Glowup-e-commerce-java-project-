package io.virinchi.glowup.service;

import io.virinchi.glowup.entity.Order;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;


    public EmailService(
            JavaMailSender mailSender
    ) {

        this.mailSender =
                mailSender;
    }


    public void sendOrderConfirmation(
            String to,
            Order order
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();


        message.setTo(to);

        message.setSubject(
                "GlowUp Nepal - Order #" +
                        order.getId() +
                        " Confirmed"
        );


        String body =

                "Hello " +
                        order.getFirstName() +
                        ",\n\n" +

                        "Thank you for shopping with GlowUp Nepal!\n\n" +

                        "Your order has been successfully placed.\n\n" +

                        "Order ID: #" +
                        order.getId() +
                        "\n" +

                        "Total Amount: Rs. " +
                        order.getTotalAmount() +
                        "\n" +

                        "Payment Method: " +
                        order.getPaymentMethod() +
                        "\n" +

                        "Delivery Address: " +
                        order.getAddress() +
                        ", " +
                        order.getCity() +
                        "\n\n" +

                        "Your order status is currently: " +
                        order.getOrderStatus() +
                        "\n\n" +

                        "Thank you for choosing GlowUp Nepal.\n\n" +

                        "GlowUp Nepal";

        message.setText(body);


        mailSender.send(
                message
        );
    }


    public void sendTestEmail(
            String to
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(to);

        message.setSubject(
                "GlowUp Email Test"
        );

        message.setText(
                "Your GlowUp email configuration is working successfully."
        );

        mailSender.send(
                message
        );
    }
}