package com.hexa.cozyhavenstay.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode; // Added
import com.fasterxml.jackson.annotation.JsonBackReference; // Added

@Entity
@Table(name = "HOTEL_AMENITIES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"hotel", "amenity"}) // Added exclusion
public class HotelAmenity {

    @EmbeddedId
    private HotelAmenityId id;

    @ManyToOne(fetch = FetchType.LAZY) // Added FetchType.LAZY
    @MapsId("hotelId")
    @JoinColumn(name = "HOTEL_ID", insertable = false, updatable = false)
    @JsonBackReference("hotel-hotelAmenities") // Added
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY) // Added FetchType.LAZY
    @MapsId("amenityId")
    @JoinColumn(name = "AMENITY_ID", insertable = false, updatable = false)
    // Assuming Amenity does not have a collection of HotelAmenity.
    // If it did, you'd need @JsonBackReference("amenity-hotelAmenities") here.
    private Amenity amenity;

    public HotelAmenity(Hotel hotel, Amenity amenity) {
        this.hotel = hotel;
        this.amenity = amenity;
        this.id = new HotelAmenityId(hotel.getHotelId(), amenity.getAmenityId());
    }
}