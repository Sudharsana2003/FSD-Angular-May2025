package com.hexa.cozyhavenstay.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.math.BigDecimal;
import java.time.LocalDateTime; // Import LocalDateTime
import java.util.Set;

@Entity
@Table(name = "ROOMS", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"HOTEL_ID", "ROOM_NUMBER"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"hotel", "bookedRoomDetails", "roomType"})
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ROOM_ID")
    private Integer roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HOTEL_ID", nullable = false)
    @JsonBackReference("hotel-rooms")
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOM_TYPE_ID", nullable = false)
    @JsonBackReference("roomType-rooms")
    private RoomType roomType;

    @Column(name = "ROOM_NUMBER", nullable = false, length = 20)
    private String roomNumber;

    @Column(name = "ROOM_SIZE_SQM", precision = 10, scale = 2)
    private BigDecimal roomSizeSqm;

    @Column(name = "ROOM_SIZE_SQFT", precision = 10, scale = 2)
    private BigDecimal roomSizeSqft;

    @Column(name = "BED_PREFERENCE", nullable = false, length = 50)
    private String bedPreference;

    // --- CHANGED: maxPeople from Byte to Integer for consistency with DTOs ---
    @Column(name = "MAX_PEOPLE", nullable = false)
    private Integer maxPeople; // Changed from Byte to Integer
    // --------------------------------------------------------------------------

    @Column(name = "BASE_FARE_PER_NIGHT", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFarePerNight;

    @Column(name = "IS_AC", nullable = false)
    private Boolean isAc;

    @Column(name = "IS_AVAILABLE", nullable = false)
    private Boolean isAvailable;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("room-bookedRoomDetails")
    private Set<BookedRoomDetail> bookedRoomDetails;

    // --- ADDED: createdAt and updatedAt fields ---
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;
    // ---------------------------------------------

    // --- ADDED: Lifecycle methods for timestamps ---
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (isAvailable == null) { // Ensure default availability if not set
            isAvailable = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    // ------------------------------------------------
}