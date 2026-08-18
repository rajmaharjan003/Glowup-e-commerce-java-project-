package io.virinchi.glowup.service;

import io.virinchi.glowup.dto.AuthResponse;
import io.virinchi.glowup.dto.LoginRequest;
import io.virinchi.glowup.dto.SignupRequest;
import io.virinchi.glowup.entity.User;
import io.virinchi.glowup.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository =
                userRepository;

        this.passwordEncoder =
                passwordEncoder;
    }


    // ==========================================
    // SIGNUP
    // ==========================================

    public AuthResponse signup(
            SignupRequest request) {


        // Check email

        if (userRepository
                .existsByEmail(request.getEmail())) {

            throw new RuntimeException(
                    "Email already registered"
            );
        }


        // Check phone

        if (userRepository
                .existsByPhoneNumber(request.getPhone())) {

            throw new RuntimeException(
                    "Phone number already registered"
            );
        }


        // Create user

        User user = new User();

        user.setFullName(
                request.getName()
        );

        user.setEmail(
                request.getEmail()
        );

        user.setPhoneNumber(
                request.getPhone()
        );


        // Hash password

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );


        // Save database

        User savedUser =
                userRepository.save(user);


        return new AuthResponse(
                "Signup successful",
                savedUser.getFullName(),
                savedUser.getEmail()
        );
    }


    // ==========================================
    // LOGIN
    // ==========================================

    public AuthResponse login(
            LoginRequest request) {


        User user =
                userRepository
                        .findByEmail(
                                request.getEmail()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Invalid email or password"
                                )
                        );


        // Check password

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );


        if (!passwordMatches) {

            throw new RuntimeException(
                    "Invalid email or password"
            );
        }


        return new AuthResponse(
                "Login successful",
                user.getFullName(),
                user.getEmail()
        );
    }
}