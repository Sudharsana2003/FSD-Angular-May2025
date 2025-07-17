// src/main/java/com/hexa/cozyhavenstay/model/Booking.java
package com.hexa.cozyhavenstay.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "BOOKINGS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "user", "hotel", "bookedRoomDetails" }) // Removed "payments" from exclude
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOKING_ID")
    private Integer bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    @JsonBackReference("user-bookings")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HOTEL_ID", nullable = false)
    @JsonBackReference("hotel-bookings")
    private Hotel hotel;

    @Column(name = "CHECK_IN_DATE", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "CHECK_OUT_DATE", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "NUMBER_OF_ADULTS", nullable = false)
    private Integer numberOfAdults;

    @Column(name = "NUMBER_OF_CHILDREN", nullable = false)
    private Integer numberOfChildren;

    public Integer getTotalGuests() {
        return (numberOfAdults != null ? numberOfAdults : 0) +
                (numberOfChildren != null ? numberOfChildren : 0);
    }

    @Column(name = "TOTAL_FARE", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalFare;

    @Column(name = "BOOKING_DATE", nullable = false, updatable = false)
    private LocalDateTime bookingDate;

    @Column(name = "BOOKING_STATUS", nullable = false, length = 20)
    private String bookingStatus;

    // ⭐ Re-added to match your DB schema and for direct tracking on Booking ⭐
    @Column(name = "PAYMENT_ID", length = 36, unique = true) // Matches DB's PAYMENT_ID
    private String paymentIdReference; // To store a unique transaction ID for wallet deduction

    @Column(name = "CANCELLATION_DATE")
    private LocalDateTime cancellationDate;

    // ⭐ Re-added to match your DB schema and for direct tracking on Booking ⭐
    @Column(name = "REFUND_AMOUNT", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    // ⭐ Re-added to match your DB schema and for direct tracking on Booking ⭐
    @Column(name = "REFUND_STATUS", length = 255)
    private String refundStatus; // e.g., PENDING, PROCESSED, FAILED

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    // ⭐ REMOVED THIS ASSOCIATION AS PER YOUR CONFIRMATION ⭐
    // @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    // @JsonManagedReference("booking-payments")
    // private Set<Payment> payments = new HashSet<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("booking-bookedRoomDetails")
    private Set<BookedRoomDetail> bookedRoomDetails = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.bookingDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.bookingStatus == null || this.bookingStatus.isEmpty()) {
            this.bookingStatus = "PENDING_PAYMENT"; // Default status for a new booking awaiting payment
        }
        // Initialize refundStatus here if you have a default for new bookings, e.g., "N/A" or null
        if (this.refundStatus == null) {
            this.refundStatus = "N/A"; // Or leave null if that's your preferred default
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}