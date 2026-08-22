package io.virinchi.glowup.dto;

public class ReviewRequest {

    private Long productId;
    private String productName;
    private String name;
    private String email;
    private int rating = 5;
    private String comment;

    public ReviewRequest() {
    }

    public ReviewRequest(Long productId, String productName, String name, String email, int rating, String comment) {
        this.productId = productId;
        this.productName = productName;
        this.name = name;
        this.email = email;
        this.rating = rating;
        this.comment = comment;
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
}
