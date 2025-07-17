// package com.hexa.cozyhavenstay.model;

package com.hexa.cozyhavenstay.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.math.BigDecimal;
import java.time.Instant; // Use Instant for UTC timestamp for payment processing

@Entity
@Table(name = "PAYMENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PAYMENT_ID")
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOKING_ID", nullable = false)
    @JsonBackReference("booking-payments")
    private Booking booking;

    @Column(name = "PAYMENT_REFERENCE_ID", nullable = false, unique = true)
    private String paymentReferenceId;

    @Column(name = "AMOUNT", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "PAYMENT_METHOD", nullable = false, length = 50)
    private String paymentMethod;

    @Column(name = "PAYMENT_STATUS", nullable = false, length = 20)
    private String paymentStatus;

    @Column(name = "PAYMENT_DATE", nullable = false)
    private Instant paymentDate;

    @Column(name = "TRANSACTION_TYPE", nullable = false, length = 20) // e.g., "DEBIT", "CREDIT", "REFUND"
    private String transactionType;

    // Optional: for refund tracking within a payment record
    @Column(name = "REFUND_DATE")
    private Instant refundDate; // ⭐ Ensure this is Instant

    @Column(name = "REFUND_AMOUNT", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "REFUND_REFERENCE_ID")
    private String refundReferenceId;

    @Column(name = "REFUND_STATUS", length = 20) // ⭐ NEW: Add refund status to Payment entity
    private String refundStatus; // e.g., "PENDING_REFUND", "REFUNDED", "REJECTED_REFUND"

    @PrePersist
    protected void onCreate() {
        this.paymentDate = Instant.now();
        if (this.paymentStatus == null || this.paymentStatus.isEmpty()) {
            this.paymentStatus = "PENDING";
        }
    }
}