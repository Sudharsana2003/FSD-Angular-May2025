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
import java.util.Set;

@Entity
@Table(name = "BOOKINGS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "hotel", "bookedRoomDetails"})
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

    public Byte getTotalGuests() {
        return (byte) ((numberOfAdults != null ? numberOfAdults : 0) +
                       (numberOfChildren != null ? numberOfChildren : 0));
    }

    @Column(name = "TOTAL_FARE", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalFare;

    @Column(name = "BOOKING_DATE", nullable = false, updatable = false)
    private LocalDateTime bookingDate;

    @Column(name = "BOOKING_STATUS", nullable = false, length = 20)
    private String bookingStatus;

    @Column(name = "PAYMENT_ID", unique = true, length = 36)
    private String paymentIdReference;

    @Column(name = "CANCELLATION_DATE")
    private LocalDateTime cancellationDate;

    @Column(name = "REFUND_AMOUNT", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    // --- NEW FIELD ADDED HERE ---
    @Column(name = "REFUND_STATUS", length = 20) // e.g., "N/A", "PENDING", "APPROVED", "REJECTED"
    private String refundStatus;
    // ----------------------------

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("booking-bookedRoomDetails")
    private Set<BookedRoomDetail> bookedRoomDetails;

    @PrePersist
    protected void onCreate() {
        this.bookingDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.bookingStatus == null || this.bookingStatus.isEmpty()) {
            this.bookingStatus = "PENDING_PAYMENT";
        }
        // --- Initialize refundStatus for new bookings ---
        if (this.refundStatus == null || this.refundStatus.isEmpty()) {
            this.refundStatus = "N/A"; // Or "NO_REFUND_APPLICABLE"
        }
        // ------------------------------------------------
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}