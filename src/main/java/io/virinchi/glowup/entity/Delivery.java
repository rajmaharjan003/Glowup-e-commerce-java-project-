package io.virinchi.glowup.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            unique = true
    )
    private Order order;


    @Column(nullable = false)
    private String method;


    @Column(nullable = false)
    private String status;


    private String trackingNumber;


    private LocalDateTime estimatedDate;


    @Column(nullable = false)
    private LocalDateTime createdAt;


    @PrePersist
    public void onCreate() {

        createdAt =
                LocalDateTime.now();

        if (status == null) {
            status = "PREPARING";
        }
    }
}