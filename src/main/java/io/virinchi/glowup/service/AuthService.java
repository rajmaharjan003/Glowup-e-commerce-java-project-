package io.virinchi.glowup.service;

import io.virinchi.glowup.dto.AuthResponse;
import io.virinchi.glowup.dto.LoginRequest;
import io.virinchi.glowup.dto.SignupRequest;
import io.virinchi.glowup.entity.User;
import io.virinchi.glowup.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // ==========================================
    // SIGNUP
    // ==========================================
    public AuthResponse signup(SignupRequest request) {
        if (request == null) {
            throw new RuntimeException("Signup request cannot be empty");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        String email = request.getEmail().trim().toLowerCase();

        // Check email uniqueness
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already registered. Please log in instead.");
        }

        // Check phone if provided
        String phone = request.getPhone() != null ? request.getPhone().trim() : null;
        if (phone != null && !phone.isEmpty()) {
            if (userRepository.existsByPhoneNumber(phone)) {
                throw new RuntimeException("Phone number is already registered.");
            }
        }

        String fullName = (request.getName() != null && !request.getName().trim().isEmpty()) 
                ? request.getName().trim() 
                : email.split("@")[0];

        // Create user
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign role: check if admin email
        if (email.contains("admin") || email.equals("admin@glowup.com") || email.equals("rajmaharjan738@gmail.com")) {
            user.setRole("ADMIN");
        } else {
            user.setRole("CUSTOMER");
        }

        // Save to database
        User savedUser = userRepository.save(user);
        log.info("New user registered successfully: id={}, email={}, role={}", savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        // Send welcome email notification asynchronously/safely
        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());
        } catch (Exception e) {
            log.warn("Could not send welcome email: {}", e.getMessage());
        }

        return new AuthResponse(
                "Account created successfully",
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                "glowup_token_" + savedUser.getId() + "_" + System.currentTimeMillis()
        );
    }

    // ==========================================
    // LOGIN
    // ==========================================
    public AuthResponse login(LoginRequest request) {
        if (request == null) {
            throw new RuntimeException("Login request cannot be empty");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        // Check password
        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches) {
            throw new RuntimeException("Invalid email or password.");
        }

        String role = user.getRole();
        if (role == null || role.trim().isEmpty() || email.contains("admin") || email.equals("admin@glowup.com") || email.equals("rajmaharjan738@gmail.com")) {
            if (email.contains("admin") || email.equals("admin@glowup.com") || email.equals("rajmaharjan738@gmail.com")) {
                role = "ADMIN";
            } else if (role == null || role.trim().isEmpty()) {
                role = "CUSTOMER";
            }
            user.setRole(role);
            userRepository.save(user);
        }

        log.info("User logged in successfully: email={}, role={}", user.getEmail(), role);

        return new AuthResponse(
                "Login successful",
                user.getFullName(),
                user.getEmail(),
                role,
                "glowup_token_" + user.getId() + "_" + System.currentTimeMillis()
        );
    }
}