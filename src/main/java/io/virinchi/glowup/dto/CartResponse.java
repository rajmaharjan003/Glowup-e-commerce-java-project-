package io.virinchi.glowup.dto;

import java.util.List;

public class CartResponse {

    private Long cartId;

    private List<CartItemResponse> items;

    private int totalItems;

    private double totalAmount;

    public CartResponse() {
    }

    public CartResponse(
            Long cartId,
            List<CartItemResponse> items,
            int totalItems,
            double totalAmount
    ) {
        this.cartId = cartId;
        this.items = items;
        this.totalItems = totalItems;
        this.totalAmount = totalAmount;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}