package io.virinchi.glowup.service;

import io.virinchi.glowup.dto.*;
import io.virinchi.glowup.entity.User;
import io.virinchi.glowup.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final Random random = new Random();

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
            throw new RuntimeException("Email address is required.");
        }
        if (request.getPassword() == null || request.getPassword().trim().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long.");
        }

        String email = request.getEmail().trim().toLowerCase();
        String phone = request.getPhone() != null ? request.getPhone().trim() : null;
        String cleanPhone = phone != null ? phone.replaceAll("[^0-9]", "") : null;

        // Check if user already exists
        User existingUser = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (existingUser != null) {
            // Check if this was an auto-created guest order account
            boolean isGuest = "GUEST".equalsIgnoreCase(existingUser.getAuthProvider())
                    || "guest_order_session".equals(existingUser.getPassword())
                    || (existingUser.getPassword() != null && !existingUser.getPassword().startsWith("$2a$") && !existingUser.getPassword().startsWith("$2b$"));

            if (isGuest) {
                // Upgrade guest account to fully registered user
                String fullName = (request.getName() != null && !request.getName().trim().isEmpty())
                        ? request.getName().trim()
                        : (existingUser.getFullName() != null ? existingUser.getFullName() : email.split("@")[0]);

                existingUser.setFullName(fullName);
                if (phone != null && !phone.isEmpty()) {
                    existingUser.setPhoneNumber(phone);
                }
                existingUser.setPassword(passwordEncoder.encode(request.getPassword().trim()));
                existingUser.setAuthProvider("LOCAL");
                existingUser.setVerified(true);

                if (email.contains("admin") || email.equals("admin@glowup.com") || email.equals("rajmaharjan738@gmail.com")) {
                    existingUser.setRole("ADMIN");
                } else if (existingUser.getRole() == null || existingUser.getRole().trim().isEmpty()) {
                    existingUser.setRole("CUSTOMER");
                }

                User savedUser = userRepository.save(existingUser);
                log.info("Guest account upgraded to registered user: id={}, email={}", savedUser.getId(), savedUser.getEmail());

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
            } else {
                throw new RuntimeException("This email (" + email + ") is already registered. Please log in instead.");
            }
        }

        // Check phone if provided
        if (phone != null && !phone.isEmpty()) {
            if (userRepository.existsByPhoneNumber(phone) || (cleanPhone != null && userRepository.existsByPhoneNumber(cleanPhone))) {
                throw new RuntimeException("Phone number (" + phone + ") is already registered.");
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
        user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        user.setAuthProvider("LOCAL");
        user.setVerified(true);

        // Assign role: check if admin email
        if (email.contains("admin") || email.equals("admin@glowup.com") || email.equals("rajmaharjan738@gmail.com")) {
            user.setRole("ADMIN");
        } else {
            user.setRole("CUSTOMER");
        }

        // Save to database
        User savedUser = userRepository.save(user);
        log.info("New user registered successfully: id={}, email={}, role={}", savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        // Send registration confirmation email
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
            throw new RuntimeException("Email or phone is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        String input = request.getEmail().trim();
        String lowerInput = input.toLowerCase();
        String cleanPhone = input.replaceAll("[^0-9]", "");

        // Support login by email (case-insensitive) or phone
        User user = userRepository.findByEmailIgnoreCase(lowerInput)
                .or(() -> userRepository.findByPhoneNumber(input))
                .or(() -> !cleanPhone.isEmpty() ? userRepository.findByPhoneNumber(cleanPhone) : java.util.Optional.empty())
                .orElseThrow(() -> new RuntimeException("No account found with " + input + ". Please check your credentials or sign up."));

        // If user was created via guest order without password setup
        if ("guest_order_session".equals(user.getPassword())) {
            throw new RuntimeException("An order was placed with this email as guest. Please click 'Create Account' (Sign Up) to set your password.");
        }

        // Check password
        boolean passwordMatches = false;
        try {
            if (user.getPassword() != null && (user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$") || user.getPassword().startsWith("$2y$"))) {
                passwordMatches = passwordEncoder.matches(
                        request.getPassword().trim(),
                        user.getPassword()
                );
            } else if (user.getPassword() != null && user.getPassword().equals(request.getPassword().trim())) {
                // Transparently upgrade legacy plaintext password to BCrypt
                passwordMatches = true;
                user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
                userRepository.save(user);
                log.info("Upgraded legacy plaintext password to BCrypt for user: {}", user.getEmail());
            }
        } catch (Exception e) {
            log.error("Password matching error for user {}: {}", user.getEmail(), e.getMessage());
        }

        if (!passwordMatches) {
            throw new RuntimeException("Incorrect password. Please try again or use Forgot Password.");
        }

        String role = user.getRole();
        if (role == null || role.trim().isEmpty() || lowerInput.contains("admin") || lowerInput.equals("admin@glowup.com") || lowerInput.equals("rajmaharjan738@gmail.com")) {
            if (lowerInput.contains("admin") || lowerInput.equals("admin@glowup.com") || lowerInput.equals("rajmaharjan738@gmail.com")) {
                role = "ADMIN";
                if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                    user.setRole("ADMIN");
                    userRepository.save(user);
                }
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

    // ==========================================
    // GOOGLE AUTHENTICATION
    // ==========================================
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Google authentication email is required");
        }

        String email = request.getEmail().trim().toLowerCase();
        String name = (request.getName() != null && !request.getName().trim().isEmpty())
                ? request.getName().trim()
                : email.split("@")[0];

        User user = userRepository.findByEmail(email).orElse(null);
        boolean isNewUser = false;

        if (user == null) {
            isNewUser = true;
            user = new User();
            user.setEmail(email);
            user.setFullName(name);
            user.setPhoneNumber(request.getPhone() != null && !request.getPhone().trim().isEmpty() ? request.getPhone().trim() : "9800000000");
            user.setPassword(passwordEncoder.encode("google_oauth_" + System.currentTimeMillis()));
            user.setAuthProvider("GOOGLE");
            user.setGoogleId(request.getGoogleId());
            user.setAvatarUrl(request.getPicture());
            user.setVerified(true);

            if (email.contains("admin") || email.equals("admin@glowup.com") || email.equals("rajmaharjan738@gmail.com")) {
                user.setRole("ADMIN");
            } else {
                user.setRole("CUSTOMER");
            }

            user = userRepository.save(user);
            log.info("Google user registered: id={}, email={}", user.getId(), user.getEmail());

            // Send registration confirmation email
            try {
                emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
            } catch (Exception e) {
                log.warn("Could not send welcome email for Google user: {}", e.getMessage());
            }
        } else {
            // Existing user - update Google info if missing
            if (request.getGoogleId() != null) user.setGoogleId(request.getGoogleId());
            if (request.getPicture() != null) user.setAvatarUrl(request.getPicture());
            user.setVerified(true);
            userRepository.save(user);
            log.info("Google user logged in: id={}, email={}", user.getId(), user.getEmail());
        }

        return new AuthResponse(
                isNewUser ? "Account created via Google" : "Google login successful",
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                "glowup_token_" + user.getId() + "_" + System.currentTimeMillis()
        );
    }

    // ==========================================
    // FORGOT PASSWORD: SEND OTP
    // ==========================================
    public String sendForgotPasswordOtp(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required for password reset");
        }

        String cleanEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new RuntimeException("No account registered with " + cleanEmail));

        // Generate 4-digit OTP
        int code = 1000 + random.nextInt(9000);
        String otp = String.valueOf(code);

        user.setResetOtp(otp);
        user.setResetOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        log.info("Generated password reset OTP for {}: {}", cleanEmail, otp);

        // Send OTP email
        emailService.sendPasswordResetOtpEmail(cleanEmail, user.getFullName(), otp);

        return "Verification code has been sent to " + cleanEmail;
    }

    // ==========================================
    // VERIFY OTP
    // ==========================================
    public boolean verifyResetOtp(String email, String otp) {
        if (email == null || otp == null) {
            throw new RuntimeException("Email and verification code are required");
        }

        String cleanEmail = email.trim().toLowerCase();
        String cleanOtp = otp.trim();

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getResetOtp() == null || !user.getResetOtp().equals(cleanOtp)) {
            throw new RuntimeException("Invalid verification code. Please check and re-enter.");
        }

        if (user.getResetOtpExpiry() == null || user.getResetOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code has expired. Please request a new code.");
        }

        return true;
    }

    // ==========================================
    // RESET PASSWORD
    // ==========================================
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        if (request == null || request.getEmail() == null || request.getOtp() == null || request.getNewPassword() == null) {
            throw new RuntimeException("All fields are required");
        }

        if (request.getNewPassword().trim().length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters long.");
        }

        String cleanEmail = request.getEmail().trim().toLowerCase();
        String cleanOtp = request.getOtp().trim();

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getResetOtp() == null || !user.getResetOtp().equals(cleanOtp)) {
            throw new RuntimeException("Invalid verification code.");
        }

        if (user.getResetOtpExpiry() == null || user.getResetOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code has expired.");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        user.setResetOtp(null);
        user.setResetOtpExpiry(null);
        userRepository.save(user);

        log.info("Password successfully reset for user: {}", cleanEmail);

        // Send confirmation email
        try {
            emailService.sendPasswordChangedNotification(cleanEmail, user.getFullName());
        } catch (Exception e) {
            log.warn("Could not send password changed confirmation email: {}", e.getMessage());
        }

        return new AuthResponse(
                "Password updated successfully",
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                "glowup_token_" + user.getId() + "_" + System.currentTimeMillis()
        );
    }
}