package io.virinchi.glowup.repository;

import io.virinchi.glowup.entity.Product;
import io.virinchi.glowup.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductOrderByCreatedAtDesc(Product product);

    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<Review> findAllByOrderByCreatedAtDesc();

    List<Review> findByProductNameIgnoreCaseOrderByCreatedAtDesc(String productName);

    List<Review> findByUser(io.virinchi.glowup.entity.User user);

    List<Review> findByUserId(Long userId);
}
