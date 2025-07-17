package com.hexa.cozyhavenstay.security;

import com.hexa.cozyhavenstay.model.Review;
import com.hexa.cozyhavenstay.model.User;
import com.hexa.cozyhavenstay.repository.ReviewRepository;
import com.hexa.cozyhavenstay.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component // Mark this as a Spring component so it can be used in SpEL expressions
public class ReviewSecurityService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ReviewSecurityService(ReviewRepository reviewRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    /**
     * Checks if the currently authenticated user is the owner of the review with the given ID.
     * This method is designed to be used in Spring Security's @PreAuthorize expressions.
     *
     * @param reviewId The ID of the review to check ownership for.
     * @return true if the authenticated user owns the review, false otherwise.
     */
    public boolean isReviewOwner(Integer reviewId) {
        // 1. Get the authenticated user's username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return false; // No authenticated user or user is anonymous
        }

        String currentUsername = authentication.getName(); // This gets the username (email in your case)

        // 2. Fetch the User entity for the authenticated user
        Optional<User> currentUserOptional = userRepository.findByUsername(currentUsername);
        if (currentUserOptional.isEmpty()) {
            return false; // Authenticated user not found in DB
        }
        User currentUser = currentUserOptional.get();

        // 3. Fetch the Review entity
        Optional<Review> reviewOptional = reviewRepository.findById(reviewId);
        if (reviewOptional.isEmpty()) {
            return false; // Review not found
        }
        Review review = reviewOptional.get();

        // 4. Compare the user IDs to determine ownership
        return review.getUser().getUserId().equals(currentUser.getUserId());
    }
}