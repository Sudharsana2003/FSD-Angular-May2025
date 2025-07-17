package com.hexa.cozyhavenstay.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "BOOKED_ROOM_DETAILS", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"BOOKING_ID", "ROOM_ID"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"booking", "room", "hotel"})
public class BookedRoomDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOKED_ROOM_ID")
    private Integer bookedRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOKING_ID", nullable = false)
    @JsonBackReference("booking-bookedRoomDetails")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOM_ID", nullable = false)
    @JsonBackReference("room-bookedRoomDetails")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HOTEL_ID", nullable = false)
    @JsonBackReference("hotel-bookedRoomDetails")
    private Hotel hotel;

    @Column(name = "FARE_AT_BOOKING", nullable = false, precision = 10, scale = 2)
    private BigDecimal fareAtBooking;

    @Column(name = "CHECK_IN_DATE")
    private LocalDate checkInDate;

    @Column(name = "CHECK_OUT_DATE")
    private LocalDate checkOutDate;
}