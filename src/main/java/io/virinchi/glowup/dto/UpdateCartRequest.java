package io.virinchi.glowup.dto;

public class UpdateCartRequest {

    private String email;

    private int quantity;


    public String getEmail() {
        return email;
    }   

    public void setEmail(String email) {
        this.email = email;
    }


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}