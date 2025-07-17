package com.hexa.cozyhavenstay.service;

import com.hexa.cozyhavenstay.dto.ReviewRequestDto;
import com.hexa.cozyhavenstay.dto.ReviewResponseDto;
import java.util.List;

public interface ReviewService {

    // Existing method, updated to return ReviewResponseDto
    List<ReviewResponseDto> getReviewsByHotelIdForPublicView(Integer hotelId);

    // Submit a new review after stay (POST /api/reviews)
    // Requires userId from the authenticated context
    ReviewResponseDto submitReview(ReviewRequestDto reviewRequestDto, Integer userId);

    // Get current user's reviews (GET /api/reviews/my-reviews)
    List<ReviewResponseDto> getMyReviews(Integer userId);

    // Update a specific review (PUT /api/reviews/{reviewId})
    // For updates, the requestDto might contain the new rating/comment.
    ReviewResponseDto updateReview(Integer reviewId, ReviewRequestDto reviewRequestDto);

    // Delete a specific review (DELETE /api/reviews/{reviewId})
    void deleteReview(Integer reviewId);

    // Optional: getReviewsByRoomIdForPublicView(Integer roomId) if reviews are room-specific
    // (Not explicitly requested in S.No. 4, but good to keep in mind)
}