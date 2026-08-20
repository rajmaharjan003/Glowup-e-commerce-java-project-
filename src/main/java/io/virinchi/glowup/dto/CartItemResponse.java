package io.virinchi.glowup.dto;

public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String description;
    private double price;
    private double discount;
    private double finalPrice;
    private int quantity;
    private double subtotal;
    private int stock;
    private String image;
    private String brand;

    // Default constructor
    public CartItemResponse() {
    }

    // Full constructor
    public CartItemResponse(
            Long id,
            Long productId,
            String productName,
            String description,
            double price,
            double discount,
            double finalPrice,
            int quantity,
            double subtotal,
            int stock,
            String image,
            String brand
    ) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.discount = discount;
        this.finalPrice = finalPrice;
        this.quantity = quantity;
        this.subtotal = subtotal;
        this.stock = stock;
        this.image = image;
        this.brand = brand;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}