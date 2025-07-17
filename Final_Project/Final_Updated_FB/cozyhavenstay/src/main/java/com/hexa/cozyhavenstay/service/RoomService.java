package com.hexa.cozyhavenstay.service;

import com.hexa.cozyhavenstay.dto.RoomDto;
import com.hexa.cozyhavenstay.model.Room; // You might need this for internal operations, but public methods return DTOs
import java.util.List;
import java.util.Optional;

public interface RoomService {
    // Public Browse methods
    List<RoomDto> getRoomsByHotelIdForPublicView(Integer hotelId);
    RoomDto getRoomByIdForPublicView(Integer roomId);

    // --- ADD THIS METHOD ---
    RoomDto addRoomToHotel(Integer hotelId, RoomDto roomDto);
    // -----------------------

    // --- ADD THIS NEW METHOD FOR UPDATING A ROOM ---
    RoomDto updateRoom(Integer roomId, RoomDto roomDto);
    
    void deleteRoom(Integer roomId);
    

}