package io.virinchi.glowup.controller;

import io.virinchi.glowup.dto.AuthResponse;
import io.virinchi.glowup.dto.LoginRequest;
import io.virinchi.glowup.dto.SignupRequest;
import io.virinchi.glowup.service.AuthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ==========================================
    // SIGNUP
    // ==========================================
    @PostMapping({"/signup", "/api/auth/signup"})
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        log.info("Received signup request for email: {}", request != null ? request.getEmail() : null);

        try {
            AuthResponse response = authService.signup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Signup error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage(), null, null, null, null));
        }
    }

    // ==========================================
    // LOGIN
    // ==========================================
    @PostMapping({"/login", "/api/auth/login"})
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        log.info("Received login request for email: {}", request != null ? request.getEmail() : null);

        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Login error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(e.getMessage(), null, null, null, null));
        }
    }
}