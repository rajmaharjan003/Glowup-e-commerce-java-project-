package io.virinchi.glowup.controller;

import io.virinchi.glowup.service.EmailService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
public class EmailController {

    private final EmailService emailService;


    public EmailController(
            EmailService emailService
    ) {

        this.emailService =
                emailService;
    }


    @PostMapping("/test")
    public ResponseEntity<String>
    testEmail(
            @RequestParam String email
    ) {

        emailService.sendTestEmail(
                email
        );

        return ResponseEntity.ok(
                "Test email sent successfully"
        );
    }
}