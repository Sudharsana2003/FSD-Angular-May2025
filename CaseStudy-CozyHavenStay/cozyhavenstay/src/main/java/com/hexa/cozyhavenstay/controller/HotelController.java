package com.hexa.cozyhavenstay.controller;

import com.hexa.cozyhavenstay.dto.HotelSearchResponseDto;
import com.hexa.cozyhavenstay.dto.HotelRoomsDetailsDto;
import com.hexa.cozyhavenstay.dto.HotelRequest;
import com.hexa.cozyhavenstay.dto.HotelDetailsDto;
import com.hexa.cozyhavenstay.service.HotelService;
import com.hexa.cozyhavenstay.service.LocationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import for @PreAuthorize
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;
    private final LocationService locationService; // Note: LocationService endpoints are now handled below for clarity.

    public HotelController(HotelService hotelService, LocationService locationService) {
        this.hotelService = hotelService;
        this.locationService = locationService;
    }

    // --- Public Browse Endpoints (configured as permitAll() in SecurityConfig) ---

    /**
     * GET /api/hotels (1.1 Browse Hotels)
     * Get All Hotels. This endpoint now serves the purpose of "Browse Hotels".
     * NOTE: Your table 1.1 is /api/hotels. Your code had /api/hotels/all.
     * I've aligned the code to match the table's path for 1.1.
     * If /api/hotels/all is desired, update table 1.1 and SecurityConfig.
     */
    @GetMapping // Matches table 1.1: GET /api/hotels
    public ResponseEntity<List<HotelDetailsDto>> getAllHotels() {
        List<HotelDetailsDto> hotels = hotelService.getAllHotels();
        if (hotels.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(hotels, HttpStatus.OK);
    }

    /**
     * GET /api/hotels/{id} (1.2 View Hotel Details)
     * Get Hotel by ID.
     */
    @GetMapping("/{id}") // Matches table 1.2: GET /api/hotels/{hotelId}
    public ResponseEntity<HotelDetailsDto> getHotelById(@PathVariable Integer id) {
        Optional<HotelDetailsDto> hotelDetailsDto = hotelService.getHotelById(id);
        return hotelDetailsDto.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    /**
     * GET /api/hotels/search (NEW! - implicit in public browse)
     * Hotel Search Endpoint.
     */
    @GetMapping("/search")
    public ResponseEntity<List<HotelSearchResponseDto>> searchHotels(
            @RequestParam String location,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam Integer numRooms) {

        if (checkInDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past.");
        }
        if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }
        if (numRooms <= 0) {
            throw new IllegalArgumentException("Number of rooms must be greater than zero.");
        }

        List<HotelSearchResponseDto> hotels = hotelService.searchHotels(
                location, checkInDate, checkOutDate, numRooms);

        if (hotels.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(hotels);
    }

    /**
     * GET /api/hotels/{hotelId}/availability (NEW! - implicit in public browse)
     * Hotel Details with Availability Endpoint.
     */
    @GetMapping("/{hotelId}/availability")
    public ResponseEntity<HotelRoomsDetailsDto> getHotelAvailability(
            @PathVariable Integer hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam Integer numAdults,
            @RequestParam Integer numChildren) {
        HotelRoomsDetailsDto details = hotelService.getHotelDetailsWithRoomAvailability(
                hotelId, checkInDate, checkOutDate, numAdults, numChildren);
        return new ResponseEntity<>(details, HttpStatus.OK);
    }

    // --- Hotel Owner & Admin CRUD Endpoints ---

    /**
     * POST /api/hotels (3.2 Add Hotel)
     * Create Hotel.
     * NOTE: Your table 3.2 is /api/owners/hotels. Your code uses /api/hotels.
     * This method needs to be aligned. If it remains here, the security is `hasRole('HOTEL_OWNER')`.
     * If you prefer `/api/owners/hotels` (as per table), move this method to `OwnerController`.
     */
    @PostMapping
    // NOTE: Role is 'HOTEL_OWNER' as per code, table uses 'OWNER'. Aligning to code.
    @PreAuthorize("hasRole('HOTEL_OWNER')") // Matches current code logic for this path
    public ResponseEntity<HotelDetailsDto> createHotel(@RequestBody HotelRequest hotelRequest) {
        HotelDetailsDto createdHotel = hotelService.createHotel(hotelRequest);
        return new ResponseEntity<>(createdHotel, HttpStatus.CREATED);
    }

    /**
     * PUT /api/hotels/{id} (3.3 Update Hotel for Owner, also Admin override)
     * Update Hotel.
     * CRITICAL SECURITY FIX: Added @hotelSecurity.isHotelOwner check for HOTEL_OWNER.
     * NOTE: Your table 3.3 is /api/owners/hotels/{hotelId}. Your code uses /api/hotels/{id}.
     * If you prefer `/api/owners/hotels/{hotelId}`, move this to `OwnerController`.
     */
    @PutMapping("/{id}")
    // NOTE: Role is 'HOTEL_OWNER' as per code, table uses 'OWNER'. Aligning to code.
    // Enhanced security to ensure HOTEL_OWNER can only update *their* hotel.
    @PreAuthorize("hasRole('ADMIN') or (hasRole('HOTEL_OWNER') and @hotelSecurity.isHotelOwner(#id))")
    public ResponseEntity<HotelDetailsDto> updateHotel(@PathVariable Integer id, @RequestBody HotelRequest hotelRequest) {
        HotelDetailsDto updatedHotel = hotelService.updateHotel(id, hotelRequest);
        return new ResponseEntity<>(updatedHotel, HttpStatus.OK);
    }

    /**
     * DELETE /api/hotels/{id} (3.4 Delete Hotel for Owner, 4.6 Delete Hotel for Admin)
     * Delete Hotel.
     * CRITICAL SECURITY FIX: Added @hotelSecurity.isHotelOwner check for HOTEL_OWNER.
     * NOTE: Your table 3.4 is /api/owners/hotels/{hotelId}. Your code uses /api/hotels/{id}.
     * If you prefer `/api/owners/hotels/{hotelId}`, move this to `OwnerController`.
     */
    @DeleteMapping("/{id}")
    // NOTE: Role is 'HOTEL_OWNER' as per code, table uses 'OWNER'. Aligning to code.
    // Enhanced security to ensure HOTEL_OWNER can only delete *their* hotel.
    @PreAuthorize("hasRole('ADMIN') or (hasRole('HOTEL_OWNER') and @hotelSecurity.isHotelOwner(#id))")
    public ResponseEntity<Void> deleteHotel(@PathVariable Integer id) {
        hotelService.deleteHotel(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // --- Hotel Owner Specific Endpoint ---

    /**
     * GET /api/hotels/owner (3.1 View Own Hotels)
     * Get Hotels Owned by Current User.
     * NOTE: Your table 3.1 is /api/owners/my-hotels. Your code uses /api/hotels/owner.
     * If you prefer `/api/owners/my-hotels`, move this method to `OwnerController`.
     */
    @GetMapping("/owner")
    // NOTE: Role is 'HOTEL_OWNER' as per code, table uses 'OWNER'. Aligning to code.
    @PreAuthorize("hasRole('HOTEL_OWNER')") // Matches table rule (with HOTEL_OWNER role)
    public ResponseEntity<List<HotelDetailsDto>> getMyHotels() {
        List<HotelDetailsDto> myHotels = hotelService.getMyHotels();
        if (myHotels.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(myHotels, HttpStatus.OK);
    }

    // Removed duplicated /locations/suggest endpoint, as it's correctly placed in LocationController.
}