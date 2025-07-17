package com.hexa.cozyhavenstay.controller;

import com.hexa.cozyhavenstay.dto.HotelSearchResponseDto;
import com.hexa.cozyhavenstay.dto.HotelRoomsDetailsDto;
import com.hexa.cozyhavenstay.dto.HotelRequest;
import com.hexa.cozyhavenstay.dto.HotelDetailsDto;
import com.hexa.cozyhavenstay.service.HotelService;
import com.hexa.cozyhavenstay.service.LocationService;
import com.hexa.cozyhavenstay.service.ReviewService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;
    private final LocationService locationService;
    private final ReviewService reviewService;

    public HotelController(HotelService hotelService, LocationService locationService, ReviewService reviewService) {
        this.hotelService = hotelService;
        this.locationService = locationService;
        this.reviewService = reviewService;
    }

    // --- Public Browse Endpoints ---

    @GetMapping
    public ResponseEntity<List<HotelDetailsDto>> getAllHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate) {

        System.out.println("Backend: getAllHotels called with:");
        System.out.println("  City: " + city);
        System.out.println("  Check-in: " + checkInDate);
        System.out.println("  Check-out: " + checkOutDate);

        List<HotelDetailsDto> hotels = hotelService.getAllHotels(city, checkInDate, checkOutDate);
        if (hotels.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(hotels, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelDetailsDto> getHotelById(@PathVariable Integer id) {
        Optional<HotelDetailsDto> hotelDetailsDto = hotelService.getHotelById(id);
        return hotelDetailsDto.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

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

    @PostMapping
    @PreAuthorize("hasRole('HOTEL_OWNER') or hasRole('ADMIN')") // ✅ FIXED
    public ResponseEntity<HotelDetailsDto> createHotel(@RequestBody HotelRequest hotelRequest) {
        HotelDetailsDto createdHotel = hotelService.createHotel(hotelRequest);
        return new ResponseEntity<>(createdHotel, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('HOTEL_OWNER') and @hotelSecurity.isHotelOwner(#id))")
    public ResponseEntity<HotelDetailsDto> updateHotel(@PathVariable Integer id, @RequestBody HotelRequest hotelRequest) {
        HotelDetailsDto updatedHotel = hotelService.updateHotel(id, hotelRequest);
        return new ResponseEntity<>(updatedHotel, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('HOTEL_OWNER') and @hotelSecurity.isHotelOwner(#id))")
    public ResponseEntity<Void> deleteHotel(@PathVariable Integer id) {
        hotelService.deleteHotel(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // --- Hotel Owner Specific ---

    @GetMapping("/owner")
    @PreAuthorize("hasRole('HOTEL_OWNER')")
    public ResponseEntity<List<HotelDetailsDto>> getMyHotels() {
        List<HotelDetailsDto> myHotels = hotelService.getMyHotels();
        if (myHotels.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(myHotels, HttpStatus.OK);
    }

    // --- Public Reviews ---

    @GetMapping("/{hotelId}/reviews")
    public ResponseEntity<?> getHotelReviews(@PathVariable Integer hotelId) {
        try {
            List<com.hexa.cozyhavenstay.dto.ReviewResponseDto> reviews = reviewService.getReviewsByHotelIdForPublicView(hotelId);
            if (reviews.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(reviews);
        } catch (com.hexa.cozyhavenstay.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected internal server error occurred: " + e.getMessage());
        }
    }
}
