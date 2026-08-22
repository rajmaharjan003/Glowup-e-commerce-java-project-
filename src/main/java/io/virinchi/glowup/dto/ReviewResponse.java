package io.virinchi.glowup.dto;

import java.time.LocalDateTime;

public class ReviewResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String name;
    private String email;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public ReviewResponse() {
    }

    public ReviewResponse(Long id, Long productId, String productName, String name, String email, int rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.name = name;
        this.email = email;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
}
