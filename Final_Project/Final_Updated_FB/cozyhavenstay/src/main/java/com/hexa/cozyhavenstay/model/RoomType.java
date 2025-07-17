package com.hexa.cozyhavenstay.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonManagedReference; // Added

import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(name = "ROOM_TYPES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"rooms"})
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ROOM_TYPE_ID")
    private Integer roomTypeId;

    @Column(name = "TYPE_NAME", nullable = false, unique = true, length = 50)
    private String typeName;

    @Column(name = "DEFAULT_ROOM_SIZE_SQM", precision = 10, scale = 2)
    private BigDecimal defaultRoomSizeSqm;

    @Column(name = "DEFAULT_ROOM_SIZE_SQFT", precision = 10, scale = 2)
    private BigDecimal defaultRoomSizeSqft;

    @Column(name = "DEFAULT_BED_PREFERENCE", length = 50)
    private String defaultBedPreference;

    @Column(name = "DEFAULT_MAX_ADULTS")
    private Byte defaultMaxAdults;

    @Column(name = "DEFAULT_MAX_CHILDREN")
    private Byte defaultMaxChildren;

    @Column(name = "DEFAULT_BASE_FARE", nullable = false, precision = 10, scale = 2)
    private BigDecimal defaultBaseFare;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "EXTRA_ADULT_CHARGE_PERCENTAGE", precision = 5, scale = 2)
    private BigDecimal extraAdultChargePercentage;

    @Column(name = "EXTRA_CHILD_CHARGE_PERCENTAGE", precision = 5, scale = 2)
    private BigDecimal extraChildChargePercentage;

    @OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("roomType-rooms") // Added
    private Set<Room> rooms;
}