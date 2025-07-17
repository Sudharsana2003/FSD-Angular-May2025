package com.hexa.cozyhavenstay.security;

import com.hexa.cozyhavenstay.model.Booking;
import com.hexa.cozyhavenstay.model.User;
import com.hexa.cozyhavenstay.model.Hotel; // ADDED: Import Hotel model
import com.hexa.cozyhavenstay.repository.BookingRepository;
import com.hexa.cozyhavenstay.repository.UserRepository;
import com.hexa.cozyhavenstay.repository.HotelRepository; // ADDED: Import HotelRepository
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component // Mark this as a Spring component so it can be injected and used in SpEL
           // expressions
public class BookingSecurityService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository; // ADDED: HotelRepository field

    public BookingSecurityService(BookingRepository bookingRepository, UserRepository userRepository,
            HotelRepository hotelRepository) { // MODIFIED: Added HotelRepository to constructor
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository; // Initialized
    }

    /**
     * Checks if the currently authenticated user is the owner of the booking with
     * the given ID.
     * This method is designed to be used in Spring Security's @PreAuthorize
     * expressions.
     *
     * @param bookingId The ID of the booking to check ownership for.
     * @return true if the authenticated user owns the booking, false otherwise.
     */
    public boolean isBookingOwner(Integer bookingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return false; // No authenticated user
        }

        String currentUsername = authentication.getName();

        Optional<User> currentUserOptional = userRepository.findByUsername(currentUsername);
        if (currentUserOptional.isEmpty()) {
            return false; // Authenticated user not found in DB
        }
        User currentUser = currentUserOptional.get();

        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            return false; // Booking not found
        }
        Booking booking = bookingOptional.get();

        return booking.getUser().getUserId().equals(currentUser.getUserId());
    }

    /**
     * Checks if the currently authenticated user is a HOTEL_OWNER and owns the
     * hotel
     * associated with the given booking ID.
     * This is used for owner-specific operations on a booking, like approving
     * refunds.
     *
     * @param bookingId The ID of the booking to check.
     * @return true if the current user is a HOTEL_OWNER and owns the hotel of the
     *         booking, false otherwise.
     */
    public boolean isBookingOwnerHotel(Integer bookingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return false; // Not authenticated
        }

        Optional<User> currentUserOptional = userRepository.findByUsername(authentication.getName());
        if (currentUserOptional.isEmpty()) {
            return false; // Current authenticated user not found in DB
        }
        User currentUser = currentUserOptional.get();

        // Check if the current user has the ROLE_HOTEL_OWNER role
        boolean isHotelOwner = currentUser.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_HOTEL_OWNER".equals(authority.getAuthority()));
        if (!isHotelOwner) {
            return false;
        }

        // Fetch the booking to get the associated hotel
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            return false; // Booking not found
        }
        Booking booking = bookingOptional.get();

        if (booking.getHotel() == null || booking.getHotel().getOwnerUser() == null) {
            return false; // Hotel or Hotel Owner not found for this booking
        }

        // Compare the current user's ID with the hotel's owner's ID
        return currentUser.getUserId().equals(booking.getHotel().getOwnerUser().getUserId());
    }

    /**
     * Checks if the currently authenticated user is a HOTEL_OWNER and owns the
     * hotel with the given ID.
     * This method is specifically for validating direct hotel ownership, used for
     * paths like /hotels/{hotelId}/bookings.
     *
     * @param hotelId The ID of the hotel to check ownership for.
     * @return true if the authenticated user is a HOTEL_OWNER and owns this hotel,
     *         false otherwise.
     */
    public boolean isHotelOwner(Integer hotelId) { // ADDED: New method to check hotel ownership by hotelId
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return false; // Not authenticated
        }

        Optional<User> currentUserOptional = userRepository.findByUsername(authentication.getName());
        if (currentUserOptional.isEmpty()) {
            return false; // Current authenticated user not found in DB
        }
        User currentUser = currentUserOptional.get();

        // Check if the current user has the ROLE_HOTEL_OWNER role
        boolean isHotelOwnerRole = currentUser.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_HOTEL_OWNER".equals(authority.getAuthority()));
        if (!isHotelOwnerRole) {
            return false;
        }

        // Fetch the Hotel entity by its ID
        Optional<Hotel> hotelOptional = hotelRepository.findById(hotelId);
        if (hotelOptional.isEmpty()) {
            return false; // Hotel not found
        }
        Hotel hotel = hotelOptional.get();

        // Check if the current user's ID matches the owner ID of the hotel
        if (hotel.getOwnerUser() == null) {
            return false; // Hotel has no owner assigned
        }

        return currentUser.getUserId().equals(hotel.getOwnerUser().getUserId());
    }
}