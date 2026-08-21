package io.virinchi.glowup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String message;
    private String name;
    private String email;
    private String role;
    private String token;

    public AuthResponse(String message, String name, String email, String role) {
        this.message = message;
        this.name = name;
        this.email = email;
        this.role = role;
        this.token = null;
    }

    public AuthResponse(String message, String name, String email) {
        this.message = message;
        this.name = name;
        this.email = email;
        this.role = (email != null && email.toLowerCase().contains("admin")) ? "ADMIN" : "CUSTOMER";
        this.token = null;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}