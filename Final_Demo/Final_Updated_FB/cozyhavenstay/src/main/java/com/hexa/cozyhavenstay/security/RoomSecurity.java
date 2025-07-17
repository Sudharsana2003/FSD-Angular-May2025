package com.hexa.cozyhavenstay.security;

import com.hexa.cozyhavenstay.model.Room;
import com.hexa.cozyhavenstay.model.User;
import com.hexa.cozyhavenstay.repository.RoomRepository;
import com.hexa.cozyhavenstay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("roomSecurity") // This annotation makes it a Spring Bean with the name "roomSecurity"
public class RoomSecurity {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Autowired
    public RoomSecurity(RoomRepository roomRepository, UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    /**
     * Checks if the currently authenticated user is the owner of the hotel that the given room belongs to.
     * @param roomId The ID of the room to check.
     * @return true if the current user is the owner, false otherwise.
     */
    public boolean isRoomOwner(Integer roomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false; // Not authenticated
        }

        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new UsernameNotFoundException("Authenticated user details not found for username: " + currentUsername));

        Optional<Room> roomOptional = roomRepository.findById(roomId);

        if (!roomOptional.isPresent()) {
            // If the room doesn't exist, we might return false or throw a ResourceNotFoundException.
            // For security, returning false (access denied) is safer than revealing existence.
            // However, it's often handled at the service layer as a ResourceNotFound.
            // For now, let's just return false. The service layer will still throw 404 if reached.
            return false;
        }

        Room room = roomOptional.get();
        if (room.getHotel() == null || room.getHotel().getOwnerUser() == null) {
            // Room not associated with a hotel or hotel has no owner, deny access.
            return false;
        }

        Integer ownerUserId = room.getHotel().getOwnerUser().getUserId();
        return currentUser.getUserId().equals(ownerUserId);
    }
}