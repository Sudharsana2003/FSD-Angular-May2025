package com.hexa.cozyhavenstay.controller;

import com.hexa.cozyhavenstay.dto.BookingRequestDto;
import com.hexa.cozyhavenstay.dto.BookingResponseDto;
import com.hexa.cozyhavenstay.exception.ResourceNotFoundException;
import com.hexa.cozyhavenstay.exception.WalletException;
import com.hexa.cozyhavenstay.model.User;
import com.hexa.cozyhavenstay.repository.UserRepository;
import com.hexa.cozyhavenstay.security.BookingSecurityService;
import com.hexa.cozyhavenstay.service.BookingService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final BookingSecurityService bookingSecurityService;

    public BookingController(BookingService bookingService, UserRepository userRepository,
            BookingSecurityService bookingSecurityService) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
        this.bookingSecurityService = bookingSecurityService;
    }

    private Integer getCurrentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException(
                    "User is not authenticated. This method should only be called after authentication.");
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Authenticated user not found in database: " + username));
        return user.getUserId();
    }

    // MODIFIED ENDPOINT: Handles booking creation with wallet deduction
    // Allows both 'USER' and 'GUEST' roles to create bookings
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER') or hasRole('GUEST')")
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequestDto requestDto) {
        try {
            // Call the service method that handles wallet deduction
            BookingResponseDto response = bookingService.createBookingAndDeductWallet(requestDto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (WalletException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); // 400 Bad Request for wallet issues
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ORIGINAL @PostMapping method - commented out as the new /create endpoint
    // with wallet deduction is typically the primary booking creation method.
    /*
     * @PostMapping
     *
     * @PreAuthorize("isAuthenticated()")
     * public ResponseEntity<?> createBookingOriginal(@Valid @RequestBody
     * BookingRequestDto requestDto) {
     * try {
     * BookingResponseDto response = bookingService.createBooking(requestDto);
     * return new ResponseEntity<>(response, HttpStatus.CREATED);
     * } catch (ResourceNotFoundException e) {
     * e.printStackTrace();
     * return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
     * } catch (IllegalArgumentException e) {
     * e.printStackTrace();
     * return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
     * } catch (IllegalStateException e) {
     * e.printStackTrace();
     * return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
     * } catch (Exception e) {
     * e.printStackTrace();
     * return new ResponseEntity<>("Internal server error: " + e.getMessage(),
     * HttpStatus.INTERNAL_SERVER_ERROR);
     * }
     * }
     */

    // EXISTING ENDPOINT: Handles booking cancellation. No change to
    // path/security,
    // but the service method will now handle refund to wallet.
    @PutMapping("/{bookingId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.isBookingOwner(#bookingId)")
    public ResponseEntity<?> cancelBooking(@PathVariable Integer bookingId) {
        try {
            BookingResponseDto cancelledBooking = bookingService.cancelBooking(bookingId); // This service method now
                                                                                           // handles wallet refund
            return new ResponseEntity<>(cancelledBooking, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to cancel booking: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // MODIFIED ENDPOINT: Handles refund approval (Admin/Hotel Owner only). The
    // service method
    // will now explicitly add to the user's wallet.
    @PutMapping("/{bookingId}/refund")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('HOTEL_OWNER') and @bookingSecurityService.isBookingOwnerHotel(#bookingId))")
    public ResponseEntity<?> approveRefund(@PathVariable Integer bookingId) {
        try {
            BookingResponseDto updatedBooking = bookingService.approveRefund(bookingId); // This service method now
                                                                                         // handles wallet credit
            return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to approve refund: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- YOUR EXISTING ENDPOINTS (UNCHANGED) ---

    // NEW ENDPOINT ADDED HERE FOR /api/bookings/user
    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserBookings() {
        try {
            Integer userId = getCurrentAuthenticatedUserId();
            // Assuming your BookingService has a method to get all bookings for a user
            List<BookingResponseDto> userBookings = bookingService.getAllBookingsForUser(userId);
            return userBookings.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : new ResponseEntity<>(userBookings, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to fetch user bookings: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/my-upcoming")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyUpcomingBookings() {
        try {
            Integer userId = getCurrentAuthenticatedUserId();
            List<BookingResponseDto> upcomingBookings = bookingService.getUpcomingBookingsForUser(userId);
            return upcomingBookings.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : new ResponseEntity<>(upcomingBookings, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to fetch upcoming bookings: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/my-past")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyPastBookings() {
        try {
            Integer userId = getCurrentAuthenticatedUserId();
            List<BookingResponseDto> pastBookings = bookingService.getPastBookingsForUser(userId);
            return pastBookings.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : new ResponseEntity<>(pastBookings, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to fetch past bookings: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.isBookingOwner(#bookingId)")
    public ResponseEntity<?> getBookingDetails(@PathVariable Integer bookingId) {
        try {
            BookingResponseDto bookingDetails = bookingService.getBookingDetailsById(bookingId);
            return new ResponseEntity<>(bookingDetails, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to fetch booking details: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.isBookingOwner(#bookingId)")
    public ResponseEntity<?> deleteBooking(@PathVariable Integer bookingId) {
        try {
            System.out.println("User or Admin attempting to delete booking with ID: " + bookingId);
            bookingService.deleteBooking(bookingId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to delete booking: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- ADMIN-SPECIFIC ENDPOINTS ---

    @GetMapping("/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllBookingsDetails() {
        try {
            List<BookingResponseDto> bookings = bookingService.getAllBookingsDetails();
            return bookings.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to fetch all bookings: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBookingHistoryForUser(
            @PathVariable Integer userId,
            @RequestParam(required = false) String status) {
        try {
            List<BookingResponseDto> bookings;
            if (status != null && !status.isEmpty()) {
                bookings = bookingService.getBookingHistoryForUser(userId, status.toUpperCase(), null, null);
            } else {
                bookings = bookingService.getBookingHistoryForUser(userId, null, null, null);
            }
            return bookings.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to fetch booking history: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{bookingId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable Integer bookingId,
            @RequestBody Map<String, String> requestBody) {
        try {
            String newStatus = requestBody.get("status");
            if (newStatus == null || newStatus.trim().isEmpty()) {
                return new ResponseEntity<>("Status field is missing or empty in the request body.",
                        HttpStatus.BAD_REQUEST);
            }

            System.out.println("Admin request to update booking " + bookingId + " status to: " + newStatus);
            BookingResponseDto updatedBooking = bookingService.updateBookingStatus(bookingId, newStatus);
            return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to update booking status: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/admin/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBookingsForAdmin(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "minFare", required = false) Double minFare,
            @RequestParam(value = "maxFare", required = false) Double maxFare) {
        try {
            List<BookingResponseDto> bookings = bookingService.getAllBookingsDetailsFiltered(status, minFare, maxFare);
            return bookings.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to fetch admin bookings with filters: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- HOTEL OWNER-SPECIFIC ENDPOINTS ---

    @GetMapping("/owners/hotels/{hotelId}/bookings")
    @PreAuthorize("hasRole('HOTEL_OWNER') and @bookingSecurityService.isHotelOwner(#hotelId)")
    public ResponseEntity<?> getBookingsForHotelOwner(@PathVariable Integer hotelId) {
        try {
            List<BookingResponseDto> bookings = bookingService.listBookingsForHotelOwner(hotelId);
            return bookings.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to fetch hotel bookings: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/eligible-for-review")
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<?> getBookingsEligibleForReview() {
        try {
            Integer userId = getCurrentAuthenticatedUserId();
            List<BookingResponseDto> eligibleBookings = bookingService.getBookingsEligibleForReview(userId);

            if (eligibleBookings.isEmpty()) {
                return ResponseEntity.noContent().build(); // 204 No Content
            }

            return ResponseEntity.ok(eligibleBookings); // 200 OK
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to fetch eligible bookings: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}