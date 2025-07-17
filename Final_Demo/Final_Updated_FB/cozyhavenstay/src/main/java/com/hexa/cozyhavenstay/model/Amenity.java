package com.hexa.cozyhavenstay.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "AMENITIES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Amenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AMENITY_ID")
    private Integer amenityId;

    @Column(name = "AMENITY_NAME", nullable = false, unique = true, length = 100)
    private String amenityName;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    // Optional: Many-to-Many relationship with Hotel via HotelAmenity join table
    // @ManyToMany(mappedBy = "amenities") // This is usually on the Hotel side if HotelAmenity is not an entity
    // private Set<Hotel> hotels;
}