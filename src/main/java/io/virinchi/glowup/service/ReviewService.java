package io.virinchi.glowup.service;

import io.virinchi.glowup.dto.ReviewRequest;
import io.virinchi.glowup.dto.ReviewResponse;
import io.virinchi.glowup.entity.Product;
import io.virinchi.glowup.entity.Review;
import io.virinchi.glowup.entity.User;
import io.virinchi.glowup.repository.ProductRepository;
import io.virinchi.glowup.repository.ReviewRepository;
import io.virinchi.glowup.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public ReviewService(
            ReviewRepository reviewRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            EmailService emailService
    ) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // ==========================================
    // SUBMIT REVIEW + EMAIL NOTIFICATION
    // ==========================================
    @Transactional
    public ReviewResponse submitReview(ReviewRequest request) {
        if (request == null) {
            throw new RuntimeException("Review request cannot be empty");
        }

        String reviewerName = request.getName() != null && !request.getName().trim().isEmpty() 
                ? request.getName().trim() 
                : "Customer";
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        String comment = request.getComment() != null ? request.getComment().trim() : "";
        int rating = Math.max(1, Math.min(5, request.getRating()));

        Product product = null;
        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId()).orElse(null);
        }
        if (product == null && request.getProductName() != null && !request.getProductName().trim().isEmpty()) {
            product = productRepository.findFirstByNameIgnoreCase(request.getProductName().trim()).orElse(null);
        }

        User user = null;
        if (!email.isEmpty()) {
            user = userRepository.findByEmail(email).orElse(null);
        }

        String finalProductName = product != null ? product.getName() : (request.getProductName() != null ? request.getProductName() : "Product");

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setProductName(finalProductName);
        review.setReviewerName(reviewerName);
        review.setReviewerEmail(email);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        log.info("New review saved with ID: {} for product: {}", savedReview.getId(), finalProductName);

        // Update product average rating if product exists
        if (product != null) {
            try {
                List<Review> productReviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(product.getId());
                double avgRating = productReviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(rating);
                product.setRating(Math.round(avgRating * 10.0) / 10.0);
                productRepository.save(product);
            } catch (Exception e) {
                log.warn("Could not update product rating: {}", e.getMessage());
            }
        }

        // Trigger email notification to reviewer and store admin
        try {
            // Send confirmation to reviewer if email is provided
            if (!email.isEmpty()) {
                emailService.sendCustomerReviewConfirmation(email, reviewerName, finalProductName, rating);
            }
            // Send alert notification to store admin
            emailService.sendAdminReviewNotification("rajmaharjan738@gmail.com", reviewerName, email, finalProductName, rating, comment);
        } catch (Exception e) {
            log.warn("Could not send review email notification: {}", e.getMessage());
        }

        return new ReviewResponse(
                savedReview.getId(),
                product != null ? product.getId() : null,
                finalProductName,
                savedReview.getReviewerName(),
                savedReview.getReviewerEmail(),
                savedReview.getRating(),
                savedReview.getComment(),
                savedReview.getCreatedAt()
        );
    }

    // ==========================================
    // GET REVIEWS FOR PRODUCT
    // ==========================================
    public List<ReviewResponse> getReviewsByProduct(Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
        List<ReviewResponse> responses = new ArrayList<>();
        for (Review r : reviews) {
            responses.add(new ReviewResponse(
                    r.getId(),
                    r.getProduct() != null ? r.getProduct().getId() : null,
                    r.getProductName(),
                    r.getReviewerName(),
                    r.getReviewerEmail(),
                    r.getRating(),
                    r.getComment(),
                    r.getCreatedAt()
            ));
        }
        return responses;
    }

    // ==========================================
    // GET ALL REVIEWS
    // ==========================================
    public List<ReviewResponse> getAllReviews() {
        List<Review> reviews = reviewRepository.findAllByOrderByCreatedAtDesc();
        List<ReviewResponse> responses = new ArrayList<>();
        for (Review r : reviews) {
            responses.add(new ReviewResponse(
                    r.getId(),
                    r.getProduct() != null ? r.getProduct().getId() : null,
                    r.getProductName(),
                    r.getReviewerName(),
                    r.getReviewerEmail(),
                    r.getRating(),
                    r.getComment(),
                    r.getCreatedAt()
            ));
        }
        return responses;
    }

    // ==========================================
    // DELETE REVIEW (FOR ADMIN)
    // ==========================================
    @Transactional
    public void deleteReview(Long id) {
        if (id != null && reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            log.info("Review #{} deleted by admin", id);
        }
    }
}
