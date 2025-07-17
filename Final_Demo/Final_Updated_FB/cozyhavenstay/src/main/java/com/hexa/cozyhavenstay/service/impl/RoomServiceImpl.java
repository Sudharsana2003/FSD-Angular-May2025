package com.hexa.cozyhavenstay.service.impl;

import com.hexa.cozyhavenstay.dto.RoomDto;
import com.hexa.cozyhavenstay.dto.RoomTypeDto;
import com.hexa.cozyhavenstay.exception.ResourceNotFoundException; // Ensure this import is present
import com.hexa.cozyhavenstay.model.Hotel;
import com.hexa.cozyhavenstay.model.Room;
import com.hexa.cozyhavenstay.model.RoomType;
import com.hexa.cozyhavenstay.model.User;
import com.hexa.cozyhavenstay.repository.HotelRepository;
import com.hexa.cozyhavenstay.repository.RoomRepository;
import com.hexa.cozyhavenstay.repository.RoomTypeRepository;
import com.hexa.cozyhavenstay.repository.UserRepository;
import com.hexa.cozyhavenstay.service.RoomService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication; // Still used for getting current user in add/update
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException; // Add this line

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final UserRepository userRepository;

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository,
                           HotelRepository hotelRepository,
                           RoomTypeRepository roomTypeRepository,
                           UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.userRepository = userRepository;
    }

    // Helper method to map Room entity to RoomDto
    private RoomDto mapToRoomDto(Room room) {
        RoomDto roomDto = new RoomDto();
        roomDto.setRoomId(room.getRoomId());
        if (room.getHotel() != null) {
            roomDto.setHotelId(room.getHotel().getHotelId());
        }
        roomDto.setRoomNumber(room.getRoomNumber());

        if (room.getRoomType() != null) {
            roomDto.setRoomType(new RoomTypeDto(
                room.getRoomType().getRoomTypeId(),
                room.getRoomType().getTypeName(),
                room.getRoomType().getDescription()
            ));
        }

        roomDto.setRoomSizeSqm(room.getRoomSizeSqm());
        roomDto.setRoomSizeSqft(room.getRoomSizeSqft());
        roomDto.setBedPreference(room.getBedPreference());
        roomDto.setMaxPeople(room.getMaxPeople());
        roomDto.setBaseFarePerNight(room.getBaseFarePerNight());
        roomDto.setIsAc(room.getIsAc());
        roomDto.setIsAvailable(room.getIsAvailable());
        return roomDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomDto> getRoomsByHotelIdForPublicView(Integer hotelId) {
        List<Room> rooms = roomRepository.findByHotelIdAndIsAvailable(hotelId);
        // Only throw ResourceNotFoundException if no rooms AND hotel doesn't exist
        if (rooms.isEmpty() && !hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel not found with ID: " + hotelId);
        }
        return rooms.stream()
                .map(this::mapToRoomDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomDto getRoomByIdForPublicView(Integer roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

        if (!room.getIsAvailable()) {
            throw new ResourceNotFoundException("Room with ID: " + roomId + " is not available for public view.");
        }

        return mapToRoomDto(room);
    }

    @Override
    @Transactional
    public RoomDto addRoomToHotel(Integer hotelId, RoomDto roomDto) {
        // Ownership check is now primarily handled by @PreAuthorize with @hotelSecurity.isHotelOwner
        // However, if you don't have a @hotelSecurity check for addRoomToHotel yet,
        // this internal check is still useful to keep as a fallback/additional layer.
        // For now, let's keep it as is, or you can remove it if you add @hotelSecurity check at controller.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Authenticated user details not found for username: " + username));

        Integer currentUserId = currentUser.getUserId();

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        boolean isAdmin = authentication.getAuthorities().stream()
                                  .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !hotel.getOwnerUser().getUserId().equals(currentUserId)) {
            // This is the AccessDeniedException previously used.
            // With @PreAuthorize custom security expressions, this internal check might become redundant
            // but is fine to keep as a defense-in-depth measure.
            throw new AccessDeniedException("You are not authorized to add rooms to this hotel as you are not its owner.");
        }

        RoomType roomType = roomTypeRepository.findById(roomDto.getRoomType().getRoomTypeId())
                                          .orElseThrow(() -> new ResourceNotFoundException("Room Type not found with ID: " + roomDto.getRoomType().getRoomTypeId()));

        Room room = new Room();
        room.setHotel(hotel);
        room.setRoomType(roomType);
        room.setRoomNumber(roomDto.getRoomNumber());
        room.setRoomSizeSqm(roomDto.getRoomSizeSqm());
        room.setRoomSizeSqft(roomDto.getRoomSizeSqft());
        room.setBedPreference(roomDto.getBedPreference());
        room.setMaxPeople(roomDto.getMaxPeople());
        room.setBaseFarePerNight(roomDto.getBaseFarePerNight());
        room.setIsAc(roomDto.getIsAc());
        room.setIsAvailable(true);

        Room savedRoom = roomRepository.save(room);

        return mapToRoomDto(savedRoom);
    }

    @Override
    @Transactional
    public RoomDto updateRoom(Integer roomId, RoomDto roomDto) {
        // The ownership check is now handled by @PreAuthorize using @roomSecurity.isRoomOwner(#roomId)
        // This means the internal check below becomes redundant if @roomSecurity.isRoomOwner is used.
        // However, for consistency and defense-in-depth, we can leave it or remove it.
        // If @PreAuthorize is doing its job, this internal check will never be reached for unauthorized users.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Authenticated user details not found for username: " + username));

        Integer currentUserId = currentUser.getUserId();

        Room existingRoom = roomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

        Hotel associatedHotel = existingRoom.getHotel();
        if (associatedHotel == null) {
            throw new IllegalStateException("Room with ID: " + roomId + " is not associated with any hotel.");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                                  .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // This internal check can be removed if @PreAuthorize for updateRoom is robustly implemented with @roomSecurity.isRoomOwner
        // For now, keeping it as it doesn't hurt, but if you want cleaner code, remove this block.
        if (!isAdmin && !associatedHotel.getOwnerUser().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to update this room as you are not the owner of its hotel.");
        }

        Optional.ofNullable(roomDto.getRoomNumber()).ifPresent(existingRoom::setRoomNumber);
        if (roomDto.getRoomType() != null && roomDto.getRoomType().getRoomTypeId() != null) {
            RoomType newRoomType = roomTypeRepository.findById(roomDto.getRoomType().getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Room Type not found with ID: " + roomDto.getRoomType().getRoomTypeId()));
            existingRoom.setRoomType(newRoomType);
        }
        Optional.ofNullable(roomDto.getRoomSizeSqm()).ifPresent(existingRoom::setRoomSizeSqm);
        Optional.ofNullable(roomDto.getRoomSizeSqft()).ifPresent(existingRoom::setRoomSizeSqft);
        Optional.ofNullable(roomDto.getBedPreference()).ifPresent(existingRoom::setBedPreference);
        Optional.ofNullable(roomDto.getMaxPeople()).ifPresent(existingRoom::setMaxPeople);
        Optional.ofNullable(roomDto.getBaseFarePerNight()).ifPresent(existingRoom::setBaseFarePerNight);
        Optional.ofNullable(roomDto.getIsAc()).ifPresent(existingRoom::setIsAc);
        Optional.ofNullable(roomDto.getIsAvailable()).ifPresent(existingRoom::setIsAvailable);

        Room updatedRoom = roomRepository.save(existingRoom);

        return mapToRoomDto(updatedRoom);
    }

    @Override
    @Transactional
    public void deleteRoom(Integer roomId) {
        // The security check (ownership + role) for deletion is now fully handled by
        // @PreAuthorize("hasRole('HOTEL_OWNER') and @roomSecurity.isRoomOwner(#roomId)")
        // in the RoomController.
        // So, this method only needs to focus on finding and deleting the room.
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

        roomRepository.delete(room);
    }
}