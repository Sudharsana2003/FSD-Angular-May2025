package com.hexa.cozyhavenstay.controller;

import com.hexa.cozyhavenstay.dto.ReviewRequestDto;
import com.hexa.cozyhavenstay.dto.ReviewResponseDto;
import com.hexa.cozyhavenstay.service.ReviewService;
import com.hexa.cozyhavenstay.exception.ResourceNotFoundException;
import com.hexa.cozyhavenstay.model.User;
import com.hexa.cozyhavenstay.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/reviews") // Base path for review-related endpoints
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @Autowired
    public ReviewController(ReviewService reviewService, UserRepository userRepository) {
        this.reviewService = reviewService;
        this.userRepository = userRepository;
    }

    // Helper method to get the current authenticated user's ID
    private Integer getCurrentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User is not authenticated. This method should only be called after authentication.");
        }
        String username = authentication.getName(); // This should be the email/username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found in database: " + username));
        return user.getUserId();
    }


    // Endpoint to get public reviews for a specific hotel (Implicitly public)
    // GET /api/reviews/public/{hotelId}
    @GetMapping("/public/{hotelId}")
    // No @PreAuthorize needed. Will be configured as permitAll() in SecurityConfig.
    public ResponseEntity<?> getReviewsByHotelId(@PathVariable Integer hotelId) {
        try {
            List<ReviewResponseDto> reviews = reviewService.getReviewsByHotelIdForPublicView(hotelId);
            if (reviews.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return ResponseEntity.ok(reviews);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.err.println("Error fetching reviews for hotelId " + hotelId + ": " + e.getMessage());
            return new ResponseEntity<String>("An unexpected internal server error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 4.1 POST /api/reviews - Submit a new review after stay.
    @PostMapping
    // NOTE: Table uses 'USER', code uses 'GUEST'. Assuming 'GUEST' is the correct role name for regular users.
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<?> submitReview(@Valid @RequestBody ReviewRequestDto requestDto) {
        try {
            Integer userId = getCurrentAuthenticatedUserId();
            ReviewResponseDto response = reviewService.submitReview(requestDto, userId);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            System.err.println("Error submitting review: " + e.getMessage());
            return new ResponseEntity<String>("An unexpected internal server error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 4.2 GET /api/reviews/my-reviews - Get current user's reviews.
    @GetMapping("/my-reviews")
    @PreAuthorize("isAuthenticated()") // Matches table rule
    public ResponseEntity<?> getMyReviews() {
        try {
            Integer userId = getCurrentAuthenticatedUserId();
            List<ReviewResponseDto> reviews = reviewService.getMyReviews(userId);
            if (reviews.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return ResponseEntity.ok(reviews);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            System.err.println("Error fetching user reviews: " + e.getMessage());
            return new ResponseEntity<String>("An unexpected internal server error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 4.3 PUT /api/reviews/{reviewId} - Update a specific review.
    // NOTE: Your code uses @reviewSecurityService, table uses @reviewSecurity. Assuming @reviewSecurityService is correct.
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN') or @reviewSecurityService.isReviewOwner(#reviewId)") // Matches table rule (with service name)
    public ResponseEntity<?> updateReview(@PathVariable Integer reviewId,
                                          @Valid @RequestBody ReviewRequestDto requestDto) {
        try {
            ReviewResponseDto updatedReview = reviewService.updateReview(reviewId, requestDto);
            return ResponseEntity.ok(updatedReview);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.err.println("Error updating review ID " + reviewId + ": " + e.getMessage());
            return new ResponseEntity<String>("An unexpected internal server error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 4.4 DELETE /api/reviews/{reviewId} - Delete a specific review.
    // NOTE: Your code uses @reviewSecurityService, table uses @reviewSecurity. Assuming @reviewSecurityService is correct.
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN') or @reviewSecurityService.isReviewOwner(#reviewId)") // Matches table rule (with service name)
    public ResponseEntity<?> deleteReview(@PathVariable Integer reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.err.println("Error deleting review ID " + reviewId + ": " + e.getMessage());
            return new ResponseEntity<String>("An unexpected internal server error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}