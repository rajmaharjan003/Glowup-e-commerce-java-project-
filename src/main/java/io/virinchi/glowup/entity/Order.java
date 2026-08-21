package io.virinchi.glowup.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phone;

    private String alternatePhone;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    private String province;

    private String deliveryNotes;

    @Column(nullable = false)
    private Double subtotal;

    @Column(nullable = false)
    private Double deliveryFee;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private String paymentStatus;

    @Column(nullable = false)
    private String orderStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items =
            new ArrayList<>();


    @PrePersist
    public void onCreate() {

        createdAt =
                LocalDateTime.now();

        if (paymentStatus == null) {
            paymentStatus =
                    "PENDING";
        }

        if (orderStatus == null) {
            orderStatus =
                    "PLACED";
        }
    }
}