package io.virinchi.glowup.controller;

import io.virinchi.glowup.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> testEmail(@RequestParam String email) {
        emailService.sendWelcomeEmail(email, "Test User");
        return ResponseEntity.ok(Map.of("message", "Welcome email sent successfully to " + email, "status", "SUCCESS"));
    }

    @PostMapping("/test-otp")
    public ResponseEntity<Map<String, String>> testOtp(@RequestParam String email) {
        emailService.sendPasswordResetOtpEmail(email, "Test User", "4829");
        return ResponseEntity.ok(Map.of("message", "OTP email sent successfully to " + email, "status", "SUCCESS"));
    }
}