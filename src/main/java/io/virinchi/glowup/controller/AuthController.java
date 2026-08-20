package io.virinchi.glowup.controller;

import io.virinchi.glowup.dto.AuthResponse;
import io.virinchi.glowup.dto.LoginRequest;
import io.virinchi.glowup.dto.SignupRequest;
import io.virinchi.glowup.service.AuthService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@CrossOrigin(
        origins = {
                "http://localhost:5500",
                "http://127.0.0.1:5500"
        }
)
public class AuthController {

    private final AuthService authService;


    public AuthController(
            AuthService authService) {

        this.authService =
                authService;
    }


    // ==========================================
    // SIGNUP
    // ==========================================

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestBody SignupRequest request) {

        log.info("signup request: {}", request);

        try {

            AuthResponse response =
                    authService.signup(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            e.getMessage()
                    );
        }
    }


    // ==========================================
    // LOGIN
    // ==========================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        try {

            AuthResponse response =
                    authService.login(request);

            return ResponseEntity
                    .ok(response);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            e.getMessage()
                    );
        }
    }
}