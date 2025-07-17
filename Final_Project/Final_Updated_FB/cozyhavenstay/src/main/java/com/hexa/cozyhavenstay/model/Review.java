package com.hexa.cozyhavenstay.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode; // Added
import com.fasterxml.jackson.annotation.JsonBackReference; // Added

import java.time.LocalDateTime;

@Entity
@Table(name = "REVIEWS", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"USER_ID", "BOOKING_ID"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "hotel", "booking"}) // Added exclusion
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REVIEW_ID")
    private Integer reviewId;

    @ManyToOne(fetch = FetchType.LAZY) // Added FetchType.LAZY
    @JoinColumn(name = "USER_ID", nullable = false)
    @JsonBackReference("user-reviews") // Added
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) // Added FetchType.LAZY
    @JoinColumn(name = "HOTEL_ID", nullable = false)
    @JsonBackReference("hotel-reviews") // Added
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY) // Added FetchType.LAZY
    @JoinColumn(name = "BOOKING_ID", unique = true)
    // No JsonBackReference needed if Booking doesn't have a collection of Reviews directly.
    // If Booking did have a Set<Review> reviews, then add @JsonBackReference("booking-reviews") here.
    private Booking booking;

    @Column(name = "RATING", nullable = false)
    private Byte rating;

    @Column(name = "COMMENT_TEXT", columnDefinition = "LONGTEXT")
    private String commentText;

    @Column(name = "REVIEW_DATE", nullable = false, updatable = false)
    private LocalDateTime reviewDate;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (reviewDate == null) {
            reviewDate = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) { // This block was slightly off, corrected logic
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}