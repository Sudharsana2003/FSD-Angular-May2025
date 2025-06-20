package com.hexa.cozyhavenstay.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PAYMENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PAYMENT_ID")
    private Integer paymentId;

    @ManyToOne
    @JoinColumn(name = "BOOKING_ID", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "AMOUNT", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "CURRENCY", nullable = false, length = 5)
    private String currency;

    @Column(name = "PAYMENT_METHOD", nullable = false, length = 50)
    private String paymentMethod;

    @Column(name = "GATEWAY_TRANSACTION_ID", unique = true, length = 100)
    private String gatewayTransactionId;

    @Column(name = "PAYMENT_STATUS", nullable = false, length = 20)
    private String paymentStatus;

    @Column(name = "PAYMENT_DATE", nullable = false, updatable = false)
    private LocalDateTime paymentDate;

    @Column(name = "CARD_LAST_FOUR_DIGITS", length = 4)
    private String cardLastFourDigits;

    @Column(name = "REFUNDED_AMOUNT", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundedAmount;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
        if (currency == null) {
            currency = "INR";
        }
        if (refundedAmount == null) {
            refundedAmount = BigDecimal.ZERO;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}