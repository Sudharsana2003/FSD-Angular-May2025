package com.hexa.cozyhavenstay.service;

import com.hexa.cozyhavenstay.dto.HotelSearchResponseDto;
import com.hexa.cozyhavenstay.dto.HotelRoomsDetailsDto;
import com.hexa.cozyhavenstay.dto.HotelRequest;
import com.hexa.cozyhavenstay.dto.HotelDetailsDto; // REQUIRED for returning detailed hotel info
import com.hexa.cozyhavenstay.model.Hotel; // Still needed for internal service logic, but not as return type

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HotelService {

    // --- CRUD operations (Public/Admin Facing) ---

    /**
     * Creates a new hotel entry.
     * @param hotelRequest The DTO containing hotel details to be saved.
     * @return The saved HotelDetailsDto.
     */
    HotelDetailsDto createHotel(HotelRequest hotelRequest); // <-- CHANGED: Returns HotelDetailsDto

    /**
     * Retrieves a hotel by its ID.
     * @param id The ID of the hotel.
     * @return An Optional containing the hotel DTO if found.
     */
    Optional<HotelDetailsDto> getHotelById(Integer id); // <-- CORRECT: Already HotelDetailsDto

    /**
     * Retrieves all hotels.
     * @return A list of all HotelDetailsDto entities.
     */
    List<HotelDetailsDto> getAllHotels(); // <-- CHANGED: Returns List<HotelDetailsDto>

    /**
     * Updates an existing hotel.
     * @param id The ID of the hotel to be updated.
     * @param hotelRequest The updated hotel details DTO.
     * @return The updated HotelDetailsDto.
     */
    HotelDetailsDto updateHotel(Integer id, HotelRequest hotelRequest); // <-- CHANGED: Returns HotelDetailsDto

    /**
     * Deletes a hotel by ID.
     * @param id The ID of the hotel to be deleted.
     */
    void deleteHotel(Integer id); // <-- NO CHANGE

    // --- Hotel Owner Specific Methods ---

    /**
     * Retrieves all hotels owned by the currently authenticated user (Hotel Owner).
     * @return A list of HotelDetailsDto entities owned by the current user.
     */
    List<HotelDetailsDto> getMyHotels(); // <-- CHANGED: Returns List<HotelDetailsDto>

    // --- Hotel Search functionality (Public Facing) ---

    /**
     * Searches for hotels by location and availability.
     * @param location The target location (e.g., city).
     * @param checkInDate The desired check-in date.
     * @param checkOutDate The desired check-out date.
     * @param numRooms The number of rooms needed.
     * @return A list of matching hotels with additional details as DTOs.
     */
    List<HotelSearchResponseDto> searchHotels(
            String location,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Integer numRooms); // <-- NO CHANGE (already returns DTO)

    /**
     * Retrieves detailed information about a specific hotel and its available rooms
     * for a given date range and occupancy, including calculated total fares.
     *
     * @param hotelId The ID of the hotel.
     * @param checkInDate The desired check-in date.
     * @param checkOutDate The desired check-out date.
     * @param numAdults The number of adults per room for fare calculation.
     * @param numChildren The number of children per room for fare calculation.
     * @return A DTO containing hotel details and a list of available rooms with calculated fares.
     */
    HotelRoomsDetailsDto getHotelDetailsWithRoomAvailability(
            Integer hotelId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Integer numAdults,
            Integer numChildren); // <-- NO CHANGE (already returns DTO)
}