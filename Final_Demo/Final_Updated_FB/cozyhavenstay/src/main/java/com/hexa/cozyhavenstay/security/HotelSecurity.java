package com.hexa.cozyhavenstay.security;

import com.hexa.cozyhavenstay.model.Hotel;
import com.hexa.cozyhavenstay.model.User; // Import your User entity
import com.hexa.cozyhavenstay.repository.HotelRepository; // Assuming you have this repository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("hotelSecurity") // Added the bean name for explicit referencing in @PreAuthorize
public class HotelSecurity {

    @Autowired
    private HotelRepository hotelRepository; // Make sure you have a HotelRepository

    /**
     * Checks if the currently authenticated user is the owner of the specified hotel.
     * This method is used in @PreAuthorize annotations.
     * @param hotelId The ID of the hotel to check ownership for.
     * @return true if the current user is the owner, false otherwise.
     */
    public boolean isHotelOwner(Integer hotelId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false; // No authenticated user
        }

        // Get the User object directly from the principal because your User entity implements UserDetails
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            // This should not happen if your CustomUserDetailsService returns your User entity
            return false;
        }
        User currentUser = (User) principal;
        Integer currentUserId = currentUser.getUserId(); // Use the getUserId() method you added

        Optional<Hotel> hotelOptional = hotelRepository.findById(hotelId);

        // Map the Optional to check if the hotel exists AND its owner matches the current user's ID
        return hotelOptional.map(hotel -> hotel.getOwnerUser() != null && hotel.getOwnerUser().getUserId().equals(currentUserId))
                            .orElse(false); // If hotel not found, or owner is null, or owner ID doesn't match, return false
    }
}