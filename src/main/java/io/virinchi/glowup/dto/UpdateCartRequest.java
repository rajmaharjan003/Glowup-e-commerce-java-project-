package io.virinchi.glowup.dto;

public class UpdateCartRequest {

    private int quantity;

    public UpdateCartRequest() {
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}