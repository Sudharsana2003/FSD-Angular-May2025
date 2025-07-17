package com.hexa.cozyhavenstay.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference; // Add this import

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "HOTELS", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"HOTEL_NAME", "LOCATION"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"rooms", "hotelAmenities", "bookings", "bookedRoomDetails", "reviews", "ownerUser"})
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HOTEL_ID")
    private Integer hotelId;

    @Column(name = "HOTEL_NAME", nullable = false, length = 255)
    private String hotelName;

    @Column(name = "LOCATION", nullable = false, length = 255)
    private String location;

    @Column(name = "ADDRESS", nullable = false, length = 500)
    private String address;

    @Column(name = "DESCRIPTION", columnDefinition = "LONGTEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OWNER_USER_ID", nullable = false)
    @JsonBackReference("user-ownedHotels") // <--- CHANGED: This is the "back" side of User's ownedHotels
    private User ownerUser;

    @Column(name = "CONTACT_COUNTRY_CODE", nullable = false, length = 5)
    private String contactCountryCode;

    @Column(name = "CONTACT_LOCAL_PHONE_NUMBER", nullable = false, length = 20)
    private String contactLocalPhoneNumber;

    @Column(name = "CONTACT_EMAIL", length = 255)
    private String contactEmail;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("hotel-rooms")
    private Set<Room> rooms;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("hotel-hotelAmenities")
    private Set<HotelAmenity> hotelAmenities;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("hotel-bookings")
    private Set<Booking> bookings;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("hotel-bookedRoomDetails")
    private Set<BookedRoomDetail> bookedRoomDetails;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("hotel-reviews")
    private Set<Review> reviews;


    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}