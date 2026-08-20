package io.virinchi.glowup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private double price;

    private double discount;

    private int stock;

    private String image;

    private String brand;

    private double rating;

    private boolean featured;

    private boolean flashSale;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;


}