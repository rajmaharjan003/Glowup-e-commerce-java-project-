package io.virinchi.glowup.controller;

import io.virinchi.glowup.service.EmailService;
import org.springframework.http.HttpStatus;
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

    @RequestMapping(value = "/test", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> testEmail(@RequestParam String email) {
        try {
            boolean sent = emailService.sendTestEmailDirect(email);
            if (sent) {
                return ResponseEntity.ok(Map.of(
                        "message", "Test email sent successfully to " + email,
                        "status", "SUCCESS",
                        "recipient", email
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "message", "Failed to send email. Check SMTP logs.",
                        "status", "ERROR"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Email send error: " + e.getMessage(),
                    "status", "ERROR"
            ));
        }
    }

    @RequestMapping(value = "/test-otp", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> testOtp(@RequestParam String email) {
        try {
            emailService.sendPasswordResetOtpEmail(email, "Test User", "4829");
            return ResponseEntity.ok(Map.of(
                    "message", "OTP email sent successfully to " + email,
                    "status", "SUCCESS",
                    "recipient", email
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "OTP email send error: " + e.getMessage(),
                    "status", "ERROR"
            ));
        }
    }
}