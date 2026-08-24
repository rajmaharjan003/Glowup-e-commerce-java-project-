package io.virinchi.glowup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {

    private Long productId;
    private String name;
    private String productName;
    private Integer quantity;
    private Double price;
    private String emoji;
    private String category;
    private String variant;
    private String image;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name != null ? name : productName;
    }

    public void setName(String name) {
        this.name = name;
        if (this.productName == null) {
            this.productName = name;
        }
    }

    public String getProductName() {
        return productName != null ? productName : name;
    }

    public void setProductName(String productName) {
        this.productName = productName;
        if (this.name == null) {
            this.name = productName;
        }
    }

    public Integer getQuantity() {
        return quantity != null && quantity > 0 ? quantity : 1;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price != null ? price : 0.0;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}