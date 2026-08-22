package io.virinchi.glowup.controller;

import io.virinchi.glowup.entity.User;
import io.virinchi.glowup.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<User> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        String newRole = body != null ? body.get("role") : "CUSTOMER";
        user.setRole(newRole);
        return ResponseEntity.ok(userRepository.save(user));
    }
}
