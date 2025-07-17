// src/main/java/com/hexa/cozyhavenstay/service/impl/BookingServiceImpl.java
package com.hexa.cozyhavenstay.service.impl;

import com.hexa.cozyhavenstay.dto.BookingRequestDto;
import com.hexa.cozyhavenstay.dto.BookingResponseDto;
import com.hexa.cozyhavenstay.exception.ResourceNotFoundException;
import com.hexa.cozyhavenstay.exception.WalletException; // ⭐ NEW IMPORT ⭐
import com.hexa.cozyhavenstay.model.Booking;
import com.hexa.cozyhavenstay.model.BookedRoomDetail;
import com.hexa.cozyhavenstay.model.Hotel;
import com.hexa.cozyhavenstay.model.Room;
import com.hexa.cozyhavenstay.model.User;
import com.hexa.cozyhavenstay.repository.BookedRoomDetailRepository;
import com.hexa.cozyhavenstay.repository.BookingRepository;
import com.hexa.cozyhavenstay.repository.HotelRepository;
import com.hexa.cozyhavenstay.repository.RoomRepository;
import com.hexa.cozyhavenstay.repository.UserRepository;
import com.hexa.cozyhavenstay.service.BookingService;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID; // ⭐ NEW IMPORT ⭐
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookedRoomDetailRepository bookedRoomDetailRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository, // ⭐ INJECT UserRepository ⭐
                              HotelRepository hotelRepository,
                              RoomRepository roomRepository,
                              BookedRoomDetailRepository bookedRoomDetailRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository; // ⭐ ASSIGNMENT ⭐
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.bookedRoomDetailRepository = bookedRoomDetailRepository;
    }

    private User getCurrentAuthenticatedUser() {
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Authenticated user not found: " + authenticatedUsername));
    }

    @Override
    @Transactional // Ensure transactionality for the entire operation
    // ⭐ RENAMED METHOD AND ADDED WalletException ⭐
    public BookingResponseDto createBookingAndDeductWallet(BookingRequestDto requestDto) throws WalletException {
        logger.info("Attempting to create booking for hotelId: {} and rooms: {}", requestDto.getHotelId(),
                requestDto.getRoomIds());
        // --- 1. Validate Input and Fetch Entities ---
        User user = getCurrentAuthenticatedUser();

        Hotel hotel = hotelRepository.findById(requestDto.getHotelId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Hotel not found with ID: " + requestDto.getHotelId()));

        // Date and guest count validations
        if (requestDto.getCheckInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past.");
        }
        if (requestDto.getCheckOutDate().isBefore(requestDto.getCheckInDate()) ||
                requestDto.getCheckOutDate().isEqual(requestDto.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }
        if (requestDto.getNumberOfAdults() <= 0 && requestDto.getNumberOfChildren() <= 0) {
            throw new IllegalArgumentException("At least one adult or child must be specified for the booking.");
        }
        if (requestDto.getRoomIds() == null || requestDto.getRoomIds().isEmpty()) {
            throw new IllegalArgumentException("At least one room ID must be provided for the booking.");
        }

        long numberOfNights = ChronoUnit.DAYS.between(requestDto.getCheckInDate(), requestDto.getCheckOutDate());
        if (numberOfNights <= 0) {
            throw new IllegalArgumentException("Number of nights must be greater than zero.");
        }

        // --- 2. Check Room Availability & Calculate Total Fare ---
        BigDecimal totalBookingFare = BigDecimal.ZERO;
        Set<BookedRoomDetail> newBookedRoomDetails = new HashSet<>();

        for (Integer roomId : new HashSet<>(requestDto.getRoomIds())) {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

            if (!room.getHotel().getHotelId().equals(hotel.getHotelId())) {
                throw new IllegalArgumentException(
                        "Room " + roomId + " does not belong to Hotel " + hotel.getHotelName());
            }

            boolean isRoomBooked = bookedRoomDetailRepository.isRoomBookedForDateRange(
                    roomId, requestDto.getCheckInDate(), requestDto.getCheckOutDate());

            if (isRoomBooked) {
                throw new IllegalStateException(
                        "Room " + room.getRoomNumber() + " (" + room.getRoomType().getTypeName() +
                                ") is already booked for the selected dates.");
            }

            // Capacity check: Each selected room must accommodate all guests.
            // If the logic is that total guests are spread across multiple rooms,
            // this check needs to be modified to sum up capacities across all selected
            // rooms.
            if (room.getMaxPeople() < (requestDto.getNumberOfAdults() + requestDto.getNumberOfChildren())) {
                throw new IllegalArgumentException("Room " + room.getRoomNumber() + " cannot accommodate " +
                        (requestDto.getNumberOfAdults() + requestDto.getNumberOfChildren()) +
                        " guests (max capacity: " + room.getMaxPeople() + ").");
            }

            BigDecimal roomFarePerNight = room.getBaseFarePerNight();
            BigDecimal currentRoomTotalFare = roomFarePerNight.multiply(new BigDecimal(numberOfNights));
            totalBookingFare = totalBookingFare.add(currentRoomTotalFare);

            BookedRoomDetail bookedRoomDetail = new BookedRoomDetail();
            bookedRoomDetail.setRoom(room);
            bookedRoomDetail.setHotel(room.getHotel());
            bookedRoomDetail.setCheckInDate(requestDto.getCheckInDate());
            bookedRoomDetail.setCheckOutDate(requestDto.getCheckOutDate());
            bookedRoomDetail.setFareAtBooking(currentRoomTotalFare);
            newBookedRoomDetails.add(bookedRoomDetail);
        }

        // ⭐ 3. Deduct Balance from User's Wallet ⭐
        // Ensure user's balance is not null and is sufficient
        if (user.getBalance() == null || user.getBalance().compareTo(totalBookingFare) < 0) {
            logger.warn("User {} has insufficient balance (current: {}) for booking fare: {}", user.getUsername(),
                    user.getBalance(), totalBookingFare);
            throw new WalletException( // ⭐ Throw WalletException here ⭐
                    "Insufficient balance. Current balance: " + user.getBalance() + ", required: " + totalBookingFare);
        }

        // Use the repository method for atomic balance deduction
        int updatedRows = userRepository.deductBalance(user.getUserId(), totalBookingFare);
        if (updatedRows == 0) {
            // This is a safety check for highly concurrent scenarios, though less likely after the initial balance check
            logger.error("Failed to deduct balance for user {}. Likely a race condition or balance changed concurrently.", user.getUserId());
            throw new WalletException("Failed to process payment due to concurrent wallet update or insufficient funds.");
        }
        // Update the in-memory user object's balance after successful deduction
        user.setBalance(user.getBalance().subtract(totalBookingFare));
        // userRepository.save(user); // No need to explicitly save user here, as the @Query @Modifying handles it
                                   // and the transaction will commit the changes made via the query.
        logger.info("Deducted {} from user {}'s balance. New balance: {}", totalBookingFare, user.getUsername(),
                user.getBalance());

        // --- 4. Create and Persist Booking ---
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setHotel(hotel);
        booking.setCheckInDate(requestDto.getCheckInDate());
        booking.setCheckOutDate(requestDto.getCheckOutDate());
        booking.setNumberOfAdults(requestDto.getNumberOfAdults());
        booking.setNumberOfChildren(requestDto.getNumberOfChildren());
        booking.setTotalFare(totalBookingFare);

        // Booking status is CONFIRMED immediately after balance deduction
        booking.setBookingStatus("CONFIRMED"); // ⭐ Set to CONFIRMED ⭐
        booking.setPaymentIdReference(UUID.randomUUID().toString()); // ⭐ Generate unique payment ID ⭐
        booking.setRefundAmount(BigDecimal.ZERO); // Initialize
        booking.setRefundStatus("N/A"); // Initialize

        booking.setBookedRoomDetails(newBookedRoomDetails);
        // Ensure bidirectional relationship is set
        newBookedRoomDetails.forEach(detail -> detail.setBooking(booking));

        Booking savedBooking = bookingRepository.save(booking);
        logger.info("Booking created successfully with ID: {}. Status: CONFIRMED. Balance deducted from user.",
                savedBooking.getBookingId());

        // --- 5. Prepare and Return Response DTO ---
        return convertToBookingResponseDto(savedBooking);
    }

    @Override
    public Optional<Booking> getBookingById(Integer bookingId) {
        logger.debug("Fetching booking by ID: {}", bookingId);
        return bookingRepository.findById(bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByUserId(Integer userId) {
        logger.debug("Fetching bookings for user ID: {}", userId);
        return bookingRepository.findByUserUserId(userId);
    }

    @Override
    @Transactional
    public BookingResponseDto cancelBooking(Integer bookingId) {
        logger.info("Attempting to cancel booking with ID: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        // Note: With no Payment entity, "REFUNDED" as a booking status implies the
        // refund has completed.
        if ("CANCELLED".equals(booking.getBookingStatus()) || "REFUNDED".equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Booking " + bookingId + " is already cancelled or refunded.");
        }

        // Assuming check-in date is after current date + 2 days for free cancellation
        if (booking.getCheckInDate().isBefore(LocalDate.now().plusDays(2))) {
            throw new IllegalStateException(
                    "Booking " + bookingId + " cannot be cancelled less than 2 days before check-in.");
        }

        // ⭐ MODIFIED: Add the booking fare back to the user's balance on cancellation ⭐
        User user = booking.getUser();
        if (user != null) {
            BigDecimal refundAmount = booking.getTotalFare();
            int updatedRows = userRepository.addBalance(user.getUserId(), refundAmount);
            if (updatedRows > 0) {
                user.setBalance(user.getBalance().add(refundAmount)); // Update in-memory object
                logger.info("Booking fare {} added back to user {}'s balance upon cancellation. New balance: {}",
                        booking.getTotalFare(), user.getUsername(), user.getBalance());
                booking.setRefundAmount(refundAmount);
                booking.setRefundStatus("PROCESSED");
            } else {
                logger.warn("Failed to add refund amount to user {}'s balance for booking {}. Balance might not be updated.", user.getUserId(), bookingId);
                // Depending on requirements, you might want to throw a custom exception here as well.
                booking.setRefundStatus("FAILED"); // Mark refund as failed
            }
        } else {
            logger.warn("User associated with booking {} not found during cancellation. Balance not refunded.",
                    bookingId);
            booking.setRefundStatus("FAILED_NO_USER"); // Specific status if user is null
        }

        booking.setBookingStatus("CANCELLED");
        booking.setCancellationDate(LocalDateTime.now());
        Booking cancelledBooking = bookingRepository.save(booking);

        logger.info("Booking {} cancelled successfully and amount refunded to user balance (if user exists).", bookingId);

        return convertToBookingResponseDto(cancelledBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        logger.debug("Fetching all bookings.");
        return bookingRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByStatus(String status) {
        return getAllBookingsDetailsFiltered(status, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllBookingsDetails() {
        return getAllBookingsDetailsFiltered(null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingHistoryForUser(Integer userId, String status, Double minFare,
            Double maxFare) {
        logger.info("Fetching booking history for user ID: {} with status: {}, minFare: {}, maxFare: {}", userId,
                status, minFare, maxFare);
        Specification<Booking> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("userId"), userId));

            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("bookingStatus"), status.trim().toUpperCase()));
            }
            if (minFare != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalFare"), minFare));
            }
            if (maxFare != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalFare"), maxFare));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<Booking> bookings = bookingRepository.findAll(spec);
        logger.info("Found {} bookings for user ID {}", bookings.size(), userId);
        return bookings.stream()
                .map(this::convertToBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllBookingsDetailsFiltered(String status, Double minFare, Double maxFare) {
        logger.info("Service Layer - Received status: {}, minFare: {}, maxFare: {}", status, minFare, maxFare);

        Specification<Booking> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.isEmpty()) {
                logger.debug("Service Layer - Adding status filter: {}", status);
                predicates.add(cb.equal(root.get("bookingStatus"), status.trim().toUpperCase()));
            }
            if (minFare != null) {
                logger.debug("Service Layer - Adding minFare filter: {}", minFare);
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalFare"), minFare));
            }
            if (maxFare != null) {
                logger.debug("Service Layer - Adding maxFare filter: {}", maxFare);
                predicates.add(cb.lessThanOrEqualTo(root.get("totalFare"), maxFare));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<Booking> allBookings = bookingRepository.findAll(spec);
        logger.info("Service Layer - Found {} bookings after applying filters.", allBookings.size());
        return allBookings.stream()
                .map(this::convertToBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingDetailsById(Integer bookingId) {
        logger.debug("Fetching booking details for ID: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));
        return convertToBookingResponseDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getUpcomingBookingsForUser(Integer userId) {
        logger.info("Fetching upcoming bookings for user ID: {}", userId);
        List<Booking> upcomingBookings = bookingRepository.findUpcomingBookingsForUser(userId, LocalDate.now());
        return upcomingBookings.stream()
                .map(this::convertToBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getPastBookingsForUser(Integer userId) {
        logger.info("Fetching past bookings for user ID: {}", userId);
        List<Booking> pastBookings = bookingRepository.findPastBookingsForUser(userId, LocalDate.now());
        return pastBookings.stream()
                .map(this::convertToBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> listBookingsForHotelOwner(Integer hotelId) {
        logger.info("Fetching bookings for hotel owner, hotel ID: {}", hotelId);
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel not found with ID: " + hotelId);
        }

        List<Booking> bookings = bookingRepository.findByHotelHotelId(hotelId);

        return bookings.stream()
                .map(this::convertToBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponseDto approveRefund(Integer bookingId) {
        logger.info("Attempting to approve refund for booking ID: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        // Check if booking is in a state that allows refund approval (e.g., CANCELLED, not already REFUNDED)
        if (!"CANCELLED".equals(booking.getBookingStatus()) && !"PENDING_REFUND".equals(booking.getBookingStatus())) {
            throw new IllegalStateException(
                    "Refund can only be approved for cancelled or pending refund bookings. Current booking status: "
                            + booking.getBookingStatus());
        }

        if ("REFUNDED".equals(booking.getBookingStatus()) || booking.getRefundStatus().equals("PROCESSED")) {
             throw new IllegalStateException("Booking " + bookingId + " is already marked as refunded or refund already processed.");
        }

        // ⭐ NEW: Add the booking fare back to the user's balance on refund approval ⭐
        User user = booking.getUser();
        if (user != null) {
            BigDecimal refundAmount = booking.getTotalFare(); // Assuming full refund
            int updatedRows = userRepository.addBalance(user.getUserId(), refundAmount);
            if (updatedRows > 0) {
                user.setBalance(user.getBalance().add(refundAmount)); // Update in-memory object
                logger.info("Booking fare {} added back to user {}'s balance upon refund approval. New balance: {}",
                        booking.getTotalFare(), user.getUsername(), user.getBalance());
                booking.setRefundAmount(refundAmount);
                booking.setRefundStatus("PROCESSED");
                booking.setBookingStatus("REFUNDED"); // ⭐ Set final status to REFUNDED ⭐
            } else {
                logger.warn("Failed to add refund amount to user {}'s balance for booking {}. Balance might not be updated.", user.getUserId(), bookingId);
                // Handle a scenario where the balance update fails (e.g., database issue)
                booking.setRefundStatus("FAILED");
            }
        } else {
            logger.warn("User associated with booking {} not found during refund approval. Balance not refunded.",
                    bookingId);
            booking.setRefundStatus("FAILED_NO_USER");
        }

        Booking refundedBooking = bookingRepository.save(booking);

        logger.info("Refund approved and booking {} marked as REFUNDED (if successful).", bookingId);

        return convertToBookingResponseDto(refundedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto updateBookingStatus(Integer bookingId, String newStatus) {
        logger.info("Attempting to update booking {} status to: {}", bookingId, newStatus);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        String normalizedNewStatus = newStatus.trim().toUpperCase();

        Set<String> allowedBookingStatuses = Set.of(
                "PENDING_PAYMENT", // This status might be less used if payment is instant from balance
                "CONFIRMED",
                "PENDING",
                "CANCELLED",
                "COMPLETED",
                "NO_SHOW",
                "REFUNDED", // Explicitly allowed
                "PENDING_REFUND" // New status for clarity when a refund process is initiated but not yet approved
        );

        if (!allowedBookingStatuses.contains(normalizedNewStatus)) {
            logger.warn("Invalid booking status provided for bookingId {}: '{}'. Allowed statuses are: {}",
                    bookingId, newStatus, String.join(", ", allowedBookingStatuses));
            throw new IllegalArgumentException("Invalid booking status provided: " + newStatus
                    + ". Allowed statuses are: " + String.join(", ", allowedBookingStatuses));
        }

        String currentStatus = booking.getBookingStatus();

        // Prevent changing from terminal states unless it's a self-transition
        if (Set.of("CANCELLED", "COMPLETED", "NO_SHOW", "REFUNDED").contains(currentStatus)) {
            if (!currentStatus.equals(normalizedNewStatus)) { // Allow self-transition, but prevent other changes
                logger.warn("Attempted invalid status transition for booking {}. From '{}' (terminal) to '{}'.",
                        bookingId, currentStatus, normalizedNewStatus);
                throw new IllegalStateException("Booking " + bookingId + " is in a terminal state ('"
                        + currentStatus + "') and cannot be changed to '" + normalizedNewStatus + "'.");
            } else {
                 logger.info("Allowed self-transition for booking {} from '{}' to '{}'.", bookingId, currentStatus, normalizedNewStatus);
            }
        }

        // Specific rules for transitions (add more as needed for your business logic)
        if ("CONFIRMED".equals(currentStatus) && (Set.of("PENDING_PAYMENT", "PENDING").contains(normalizedNewStatus))) {
            logger.warn(
                    "Attempted invalid status transition for booking {}. From '{}' to '{}'. Cannot revert confirmed status.",
                    bookingId, currentStatus, normalizedNewStatus);
            throw new IllegalStateException("Cannot revert confirmed booking " + bookingId + " status from '"
                    + currentStatus + "' to '" + normalizedNewStatus + "'.");
        }

        // Add specific logic for "PENDING_REFUND" if it's a distinct intermediate state
        if ("CANCELLED".equals(currentStatus) && "PENDING_REFUND".equals(normalizedNewStatus)) {
            // This could be a valid transition if CANCELLED is just the first step
            // and PENDING_REFUND is when refund processing begins.
            logger.info("Transitioning booking {} from CANCELLED to PENDING_REFUND.", bookingId);
        } else if ("PENDING_REFUND".equals(currentStatus) && "REFUNDED".equals(normalizedNewStatus)) {
            logger.info("Transitioning booking {} from PENDING_REFUND to REFUNDED.", bookingId);
            // This is where approveRefund() could be called internally or ensure refund logic has run
        }


        booking.setBookingStatus(normalizedNewStatus);
        Booking updatedBooking = bookingRepository.save(booking);
        logger.info("Booking {} status updated from '{}' to '{}' successfully.", bookingId, currentStatus,
                normalizedNewStatus);
        return convertToBookingResponseDto(updatedBooking);
    }

    @Override
    @Transactional
    public void deleteBooking(Integer bookingId) {
        logger.info("Attempting to delete booking with ID: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        // If a booking is confirmed and in the future, it cannot be deleted directly.
        // It must be cancelled first.
        if ("CONFIRMED".equals(booking.getBookingStatus()) && booking.getCheckInDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException(
                    "Cannot delete confirmed future booking " + bookingId + ". Please cancel it first.");
        }

        // If the booking is in a state where a refund *might* have happened, prevent
        // deletion.
        // This implies that if bookingStatus is CANCELLED or REFUNDED, you shouldn't
        // delete without careful consideration.
        if (Set.of("CANCELLED", "REFUNDED").contains(booking.getBookingStatus())) {
            throw new IllegalStateException(
                    "Cannot delete booking " + bookingId + " because its status is '" + booking.getBookingStatus() +
                            "'. This implies a transaction has occurred (deduction/refund). Consider archiving instead.");
        }

        bookingRepository.delete(booking);
        logger.info("Booking {} deleted successfully.", bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllBookingsForUser(Integer userId) {
        logger.info("Fetching all bookings for user ID: {}", userId);
        List<Booking> bookings = bookingRepository.findByUserUserId(userId);
        return bookings.stream()
                .map(this::convertToBookingResponseDto)
                .collect(Collectors.toList());
    }

    // Private Helper Method for DTO Conversion
    private BookingResponseDto convertToBookingResponseDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setBookingId(booking.getBookingId());

        if (booking.getHotel() != null) {
            dto.setHotelId(booking.getHotel().getHotelId());
            dto.setHotelName(booking.getHotel().getHotelName());
        } else {
            dto.setHotelId(null);
            dto.setHotelName(null);
        }

        dto.setUserName(
                booking.getUser() != null
                        ? (booking.getUser().getFirstName() + " " + booking.getUser().getLastName())
                        : null);

        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setTotalFare(booking.getTotalFare());
        dto.setBookingStatus(booking.getBookingStatus());
        dto.setBookingDate(booking.getBookingDate());

        // ⭐ MODIFIED: Use refundStatus field from Booking entity directly ⭐
        dto.setRefundStatus(booking.getRefundStatus());
        dto.setRefundAmount(booking.getRefundAmount()); // Include refund amount in DTO

        dto.setNumberOfAdults(booking.getNumberOfAdults());
        dto.setNumberOfChildren(booking.getNumberOfChildren());

        if (booking.getBookedRoomDetails() != null && !booking.getBookedRoomDetails().isEmpty()) {
            dto.setBookedRoomNumbersAndTypes(
                    booking.getBookedRoomDetails().stream()
                            .map(detail -> {
                                String roomNum = (detail.getRoom() != null)
                                        ? String.valueOf(detail.getRoom().getRoomNumber())
                                        : "N/A";
                                String roomType = (detail.getRoom() != null && detail.getRoom().getRoomType() != null)
                                        ? detail.getRoom().getRoomType().getTypeName()
                                        : "N/A";
                                return "Room " + roomNum + " (" + roomType + ")";
                            })
                            .collect(Collectors.toList()));
        } else {
            dto.setBookedRoomNumbersAndTypes(new ArrayList<>());
        }

        dto.setRazorpayOrderId(booking.getPaymentIdReference()); // Use paymentIdReference for razorpayOrderId in DTO

        return dto;
    }

    @Override
    public List<BookingResponseDto> getBookingsEligibleForReview(Integer userId) {
        List<Booking> bookings = bookingRepository.findCompletedBookingsNotReviewedByUser(userId);
        return bookings.stream()
                .map(this::convertToBookingResponseDto)
                .collect(Collectors.toList());
    }
}