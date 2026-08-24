package io.virinchi.glowup.controller;

import io.virinchi.glowup.dto.*;
import io.virinchi.glowup.service.AuthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    // ==========================================
    // GOOGLE AUTHENTICATION
    // ==========================================
    @PostMapping({"/google", "/api/auth/google", "/api/auth/oauth/google"})
    public ResponseEntity<AuthResponse> googleAuth(@RequestBody GoogleAuthRequest request) {
        log.info("Received Google auth request for email: {}", request != null ? request.getEmail() : null);

        try {
            AuthResponse response = authService.googleAuth(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Google auth error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage(), null, null, null, null));
        }
    }

    // ==========================================
    // FORGOT PASSWORD - SEND OTP
    // ==========================================
    @PostMapping({"/forgot-password", "/api/auth/forgot-password"})
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            String message = authService.sendForgotPasswordOtp(request.getEmail());
            return ResponseEntity.ok(Map.of("message", message, "status", "SUCCESS"));
        } catch (RuntimeException e) {
            log.error("Forgot password error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "status", "ERROR"));
        }
    }

    // ==========================================
    // VERIFY RESET OTP
    // ==========================================
    @PostMapping({"/verify-otp", "/api/auth/verify-otp"})
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        try {
            boolean valid = authService.verifyResetOtp(request.getEmail(), request.getOtp());
            return ResponseEntity.ok(Map.of("valid", valid, "message", "Code verified successfully.", "status", "SUCCESS"));
        } catch (RuntimeException e) {
            log.error("Verify OTP error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", e.getMessage(), "status", "ERROR"));
        }
    }

    // ==========================================
    // RESET PASSWORD
    // ==========================================
    @PostMapping({"/reset-password", "/api/auth/reset-password"})
    public ResponseEntity<AuthResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            AuthResponse response = authService.resetPassword(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Reset password error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage(), null, null, null, null));
        }
    }
}