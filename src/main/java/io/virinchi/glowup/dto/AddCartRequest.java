package io.virinchi.glowup.dto;

public class AddCartRequest {

    private String email;

    private String productName;

    private int quantity;


    // ==========================================
    // GET EMAIL
    // ==========================================

    public String getEmail() {
        return email;
    }


    // ==========================================
    // SET EMAIL
    // ==========================================

    public void setEmail(String email) {
        this.email = email;
    }


    // ==========================================
    // GET PRODUCT NAME
    // ==========================================

    public String getProductName() {
        return productName;
    }


    // ==========================================
    // SET PRODUCT NAME
    // ==========================================

    public void setProductName(String productName) {
        this.productName = productName;
    }


    // ==========================================
    // GET QUANTITY
    // ==========================================

    public int getQuantity() {
        return quantity;
    }


    // ==========================================
    // SET QUANTITY
    // ==========================================

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}