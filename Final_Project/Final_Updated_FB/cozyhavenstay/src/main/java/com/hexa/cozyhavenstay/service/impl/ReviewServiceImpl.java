package com.hexa.cozyhavenstay.service.impl;

import com.hexa.cozyhavenstay.dto.ReviewRequestDto;
import com.hexa.cozyhavenstay.dto.ReviewResponseDto;
import com.hexa.cozyhavenstay.model.Booking; // Import Booking entity
import com.hexa.cozyhavenstay.model.Review;
import com.hexa.cozyhavenstay.model.Hotel;
import com.hexa.cozyhavenstay.model.User; // Import User entity
import com.hexa.cozyhavenstay.repository.ReviewRepository;
import com.hexa.cozyhavenstay.repository.HotelRepository;
import com.hexa.cozyhavenstay.repository.UserRepository; // Import UserRepository
import com.hexa.cozyhavenstay.repository.BookingRepository; // Import BookingRepository
import com.hexa.cozyhavenstay.service.ReviewService;
import com.hexa.cozyhavenstay.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate; // For date comparisons
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository; // Inject UserRepository
    private final BookingRepository bookingRepository; // Inject BookingRepository

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             HotelRepository hotelRepository,
                             UserRepository userRepository, // Add to constructor
                             BookingRepository bookingRepository) { // Add to constructor
        this.reviewRepository = reviewRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository; // Initialize
        this.bookingRepository = bookingRepository; // Initialize
    }

    // Helper method to map Review entity to ReviewResponseDto
    private ReviewResponseDto mapToReviewResponseDto(Review review) {
        ReviewResponseDto reviewResponseDto = new ReviewResponseDto();
        reviewResponseDto.setReviewId(review.getReviewId());
        reviewResponseDto.setRating(review.getRating());
        reviewResponseDto.setCommentText(review.getCommentText());
        reviewResponseDto.setReviewDate(review.getReviewDate());
        reviewResponseDto.setIsActive(review.getIsActive());

        if (review.getHotel() != null) {
            reviewResponseDto.setHotelId(review.getHotel().getHotelId());
            reviewResponseDto.setHotelName(review.getHotel().getHotelName());
        }

        if (review.getUser() != null) {
            reviewResponseDto.setUserId(review.getUser().getUserId());
            // Assuming userName should be a display name, e.g., first name + last initial
            reviewResponseDto.setUserName(review.getUser().getFirstName() + " " + review.getUser().getLastName().charAt(0) + ".");
            // If you prefer full username (email): reviewResponseDto.setUserName(review.getUser().getUsername());
        }

        if (review.getBooking() != null) {
            reviewResponseDto.setBookingId(review.getBooking().getBookingId());
        }

        return reviewResponseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByHotelIdForPublicView(Integer hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        return reviewRepository.findByHotelAndIsActiveTrueOrderByReviewDateDesc(hotel).stream()
                .map(this::mapToReviewResponseDto) // Use the updated mapping method
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewResponseDto submitReview(ReviewRequestDto requestDto, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Hotel hotel = hotelRepository.findById(requestDto.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + requestDto.getHotelId()));

        Booking booking = bookingRepository.findById(requestDto.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + requestDto.getBookingId()));

        // --- Business Logic: Verify Completed Stay and User Ownership of Booking ---
        if (!booking.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Booking with ID " + requestDto.getBookingId() + " does not belong to the authenticated user.");
        }
        if (!booking.getHotel().getHotelId().equals(requestDto.getHotelId())) {
             throw new IllegalArgumentException("Booking with ID " + requestDto.getBookingId() + " is not for Hotel ID " + requestDto.getHotelId() + ".");
        }
        if (booking.getCheckOutDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException("Review can only be submitted after the stay has been completed (check-out date has passed).");
        }
        // Optional: Check if a review already exists for this user and booking to prevent multiple reviews for one stay
        // if (reviewRepository.findByBookingBookingIdAndUserUserId(requestDto.getBookingId(), userId).isPresent()) {
        //     throw new IllegalStateException("A review for this booking has already been submitted by this user.");
        // }
        // For this, you would need to add a method to ReviewRepository: Optional<Review> findByBookingBookingIdAndUserUserId(Integer bookingId, Integer userId);
        // -------------------------------------------------------------------------

        Review review = new Review();
        review.setUser(user);
        review.setHotel(hotel);
        review.setBooking(booking); // Associate review with the booking
        review.setRating(requestDto.getRating());
        review.setCommentText(requestDto.getCommentText());
        // reviewDate, isActive, createdAt, updatedAt are handled by @PrePersist hooks in Review entity

        Review savedReview = reviewRepository.save(review);
        return mapToReviewResponseDto(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getMyReviews(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Assuming you'll add this method to your ReviewRepository
        // List<Review> reviews = reviewRepository.findByUserAndIsActiveTrueOrderByReviewDateDesc(user);
        // For now, let's use a method that finds by user ID (which you have in ReviewRepository)
        List<Review> reviews = reviewRepository.findByUserUserIdOrderByReviewDateDesc(userId); // CORRECTED LINE

        return reviews.stream()
                .filter(Review::getIsActive) // Filter for active reviews if findByUserUserId doesn't do it
                .sorted((r1, r2) -> r2.getReviewDate().compareTo(r1.getReviewDate())) // Sort by date desc
                .map(this::mapToReviewResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewResponseDto updateReview(Integer reviewId, ReviewRequestDto requestDto) {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        // Update fields that are allowed to be changed
        existingReview.setRating(requestDto.getRating());
        existingReview.setCommentText(requestDto.getCommentText());
        // updatedAt is handled by @PreUpdate hook

        Review updatedReview = reviewRepository.save(existingReview);
        return mapToReviewResponseDto(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Integer reviewId) {
        Review reviewToDelete = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        // Perform a soft delete by setting isActive to false
        reviewToDelete.setIsActive(false);
        // updatedAt will be handled by @PreUpdate hook

        reviewRepository.save(reviewToDelete);
        // If you truly want to delete: reviewRepository.delete(reviewToDelete);
    }
}