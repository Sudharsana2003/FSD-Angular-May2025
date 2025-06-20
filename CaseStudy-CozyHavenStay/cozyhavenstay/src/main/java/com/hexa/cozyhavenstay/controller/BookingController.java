package com.hexa.cozyhavenstay.controller;

import com.hexa.cozyhavenstay.dto.BookingRequestDto;
import com.hexa.cozyhavenstay.dto.BookingResponseDto;
import com.hexa.cozyhavenstay.model.User;
import com.hexa.cozyhavenstay.repository.UserRepository;
import com.hexa.cozyhavenstay.service.BookingService;
import com.hexa.cozyhavenstay.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import for @PreAuthorize
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    @Autowired
    public BookingController(BookingService bookingService, UserRepository userRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    // Helper method (no security annotation needed here, as it's a private helper)
    private Integer getCurrentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User is not authenticated. This method should only be called after authentication.");
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found in database: " + username));
        return user.getUserId();
    }

    @PostMapping // 2.3 Book Hotel
    @PreAuthorize("isAuthenticated()") // Matches table rule
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequestDto requestDto) {
        try {
            BookingResponseDto response = bookingService.createBooking(requestDto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<String>("An unexpected internal server error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/bookings/my-upcoming (2.4 Upcoming Bookings)
     * Get all upcoming bookings for the current authenticated user.
     */
    @GetMapping("/my-upcoming")
    @PreAuthorize("isAuthenticated()") // Matches table rule
    public ResponseEntity<?> getMyUpcomingBookings() {
        try {
            Integer userId = getCurrentAuthenticatedUserId();
            List<BookingResponseDto> upcomingBookings = bookingService.getUpcomingBookingsForUser(userId);
            if (upcomingBookings.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(upcomingBookings, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<String>("An unexpected internal server error occurred while fetching upcoming bookings: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/bookings/my-past (2.5 Past Bookings)
     * Get all past bookings for the current authenticated user.
     */
    @GetMapping("/my-past")
    @PreAuthorize("isAuthenticated()") // Matches table rule
    public ResponseEntity<?> getMyPastBookings() {
        try {
            Integer userId = getCurrentAuthenticatedUserId();
            List<BookingResponseDto> pastBookings = bookingService.getPastBookingsForUser(userId);
            if (pastBookings.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(pastBookings, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<String>("An unexpected internal server error occurred while fetching past bookings: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{bookingId}") // 2.6 Booking Details
    // NOTE: Your code uses @bookingSecurityService, table uses @bookingSecurity.
    // Assuming @bookingSecurityService is the correct bean name you've implemented.
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.isBookingOwner(#bookingId)")
    public ResponseEntity<?> getBookingDetails(@PathVariable Integer bookingId) {
        try {
            BookingResponseDto bookingDetails = bookingService.getBookingDetailsById(bookingId);
            return new ResponseEntity<>(bookingDetails, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<String>("An unexpected internal server error occurred while fetching booking details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{bookingId}/cancel") // 2.7 Cancel Booking
    // NOTE: Your code uses @bookingSecurityService, table uses @bookingSecurity.
    // Assuming @bookingSecurityService is the correct bean name you've implemented.
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.isBookingOwner(#bookingId)")
    public ResponseEntity<?> cancelBooking(@PathVariable Integer bookingId) {
        try {
            BookingResponseDto cancelledBooking = bookingService.cancelBooking(bookingId);
            return new ResponseEntity<>(cancelledBooking, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<String>("An unexpected internal server error occurred while canceling booking: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/bookings/details (Missing from table explicitly, but relates to 4.5 See all bookings)
     * Endpoint to retrieve details for all bookings in the system.
     * This is typically used for administrative purposes and requires ADMIN role.
     * Recommendation: Consider moving this to AdminController if you want all Admin endpoints consolidated under /api/admin.
     */
    @GetMapping("/details")
    @PreAuthorize("hasRole('ADMIN')") // Correct annotation for an admin endpoint
    public ResponseEntity<?> getAllBookingsDetails() {
        try {
            List<BookingResponseDto> bookings = bookingService.getAllBookingsDetails();
            if (bookings.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>("An unexpected internal server error occurred while fetching all booking details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/bookings/user/{userId} (Missing from table explicitly)
     * Endpoint to retrieve all bookings for a specific user.
     * Accessible by ADMIN.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')") // Correct annotation for an admin endpoint
    public ResponseEntity<?> getBookingHistoryForUser(@PathVariable Integer userId) {
        try {
            List<BookingResponseDto> bookings = bookingService.getBookingHistoryForUser(userId);
            if (bookings.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<String>("An unexpected internal server error occurred while fetching booking history: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/bookings/owners/hotels/{hotelId}/bookings (3.8 View Hotel Bookings)
     * Lists all bookings associated with a specific hotel, accessible by the hotel owner.
     * The @PreAuthorize uses the @hotelSecurity bean to verify ownership.
     */
    @GetMapping("/owners/hotels/{hotelId}/bookings")
    // NOTE: Table uses 'OWNER', code uses 'HOTEL_OWNER'. Assuming HOTEL_OWNER is correct role name.
    @PreAuthorize("hasRole('HOTEL_OWNER') and @hotelSecurity.isHotelOwner(#hotelId)") // Matches table rule (with HOTEL_OWNER role)
    public ResponseEntity<?> getBookingsForHotelOwner(@PathVariable Integer hotelId) {
        try {
            List<BookingResponseDto> bookings = bookingService.listBookingsForHotelOwner(hotelId);
            if (bookings.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<String>("An unexpected internal server error occurred while fetching hotel bookings: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * PUT /api/bookings/{bookingId}/refund (3.9 Approve Refund)
     * Approves a refund for a previously cancelled booking if the current user is an ADMIN
     * or a HOTEL_OWNER who owns the hotel associated with the booking.
     */
    @PutMapping("/{bookingId}/refund")
    // NOTE: Table 3.9 only shows 'OWNER' role and '@bookingSecurity'. Code includes 'ADMIN' and uses '@bookingSecurityService'.
    // The code's security is more robust here. Update table 3.9 to reflect this:
    // Security Annotation: `hasRole('ADMIN') or (hasRole('HOTEL_OWNER') and @bookingSecurityService.isBookingOwnerHotel(#bookingId))`
    @PreAuthorize("hasRole('ADMIN') or (hasRole('HOTEL_OWNER') and @bookingSecurityService.isBookingOwnerHotel(#bookingId))")
    public ResponseEntity<?> approveRefund(@PathVariable Integer bookingId) {
        try {
            BookingResponseDto updatedBooking = bookingService.approveRefund(bookingId);
            return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<String>("An unexpected internal server error occurred while approving refund: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}