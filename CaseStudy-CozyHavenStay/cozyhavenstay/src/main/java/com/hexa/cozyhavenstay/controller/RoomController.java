package com.hexa.cozyhavenstay.controller;

import com.hexa.cozyhavenstay.dto.RoomDto;
import com.hexa.cozyhavenstay.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api") // Base path for all endpoints in this controller
public class RoomController {

    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // Public endpoint to get rooms by hotel ID (Table 1.3: /api/hotels/{hotelId}/rooms)
    @GetMapping("/hotels/{hotelId}/rooms") // <-- MODIFIED: Removed '/public'
    public ResponseEntity<List<RoomDto>> getRoomsByHotelIdForPublicView(@PathVariable Integer hotelId) {
        List<RoomDto> rooms = roomService.getRoomsByHotelIdForPublicView(hotelId);
        if (rooms.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(rooms);
    }

    // Public endpoint to get a single room by ID (Table 1.4: /api/rooms/{roomId})
    @GetMapping("/rooms/{roomId}") // <-- MODIFIED: Removed '/public'
    public ResponseEntity<RoomDto> getRoomByIdForPublicView(@PathVariable Integer roomId) {
        RoomDto room = roomService.getRoomByIdForPublicView(roomId);
        return ResponseEntity.ok(room);
    }

    // Endpoint for owners/admins to add a room to a hotel (Table 3.5: Add Room)
    @PostMapping("/owners/hotels/{hotelId}/rooms")
    // Using 'HOTEL_OWNER' as per your confirmation.
    // Ensure @hotelSecurity is the correct bean name (or change table to @hotelSecurityService)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('HOTEL_OWNER') and @hotelSecurity.isHotelOwner(#hotelId))")
    public ResponseEntity<RoomDto> addRoomToHotel(@PathVariable Integer hotelId, @RequestBody RoomDto roomDto) {
        RoomDto createdRoom = roomService.addRoomToHotel(hotelId, roomDto);
        return new ResponseEntity<>(createdRoom, HttpStatus.CREATED);
    }

    // Endpoint to Update a Room for Owners/Admins (Table 3.6: Update Room)
    @PutMapping("/owners/rooms/{roomId}")
    // Using 'HOTEL_OWNER' as per your confirmation.
    // Ensure @roomSecurity is the correct bean name (or change table to @roomSecurityService)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('HOTEL_OWNER') and @roomSecurity.isRoomOwner(#roomId))")
    public ResponseEntity<RoomDto> updateRoom(@PathVariable Integer roomId, @RequestBody RoomDto roomDto) {
        RoomDto updatedRoom = roomService.updateRoom(roomId, roomDto);
        return ResponseEntity.ok(updatedRoom);
    }

    // Endpoint to Delete a Room for Owners/Admins (Table 3.7: Delete Room)
    @DeleteMapping("/owners/rooms/{roomId}")
    // Using 'HOTEL_OWNER' as per your confirmation.
    // Ensure @roomSecurity is the correct bean name (or change table to @roomSecurityService)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('HOTEL_OWNER') and @roomSecurity.isRoomOwner(#roomId))")
    public ResponseEntity<Void> deleteRoom(@PathVariable Integer roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }
}