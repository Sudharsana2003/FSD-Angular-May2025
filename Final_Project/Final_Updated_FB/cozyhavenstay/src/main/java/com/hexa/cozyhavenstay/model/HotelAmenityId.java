package com.hexa.cozyhavenstay.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelAmenityId implements Serializable {

    @Column(name = "HOTEL_ID")
    private Integer hotelId;

    @Column(name = "AMENITY_ID")
    private Integer amenityId;
}