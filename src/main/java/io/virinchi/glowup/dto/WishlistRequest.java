package io.virinchi.glowup.dto;

public class WishlistRequest {

    private String email;
    private Long productId;
    private String productName;

    public WishlistRequest() {
    }

    public WishlistRequest(String email, Long productId, String productName) {
        this.email = email;
        this.productId = productId;
        this.productName = productName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
