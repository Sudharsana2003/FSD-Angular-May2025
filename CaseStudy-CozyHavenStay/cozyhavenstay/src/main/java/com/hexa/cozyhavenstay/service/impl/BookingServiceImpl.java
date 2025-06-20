package com.hexa.cozyhavenstay.service.impl;

import com.hexa.cozyhavenstay.dto.BookingRequestDto;
import com.hexa.cozyhavenstay.dto.BookingResponseDto;
import com.hexa.cozyhavenstay.model.Booking;
import com.hexa.cozyhavenstay.model.BookedRoomDetail;
import com.hexa.cozyhavenstay.model.Hotel;
import com.hexa.cozyhavenstay.model.Room;
import com.hexa.cozyhavenstay.model.User;
import com.hexa.cozyhavenstay.repository.BookingRepository;
import com.hexa.cozyhavenstay.repository.BookedRoomDetailRepository;
import com.hexa.cozyhavenstay.repository.HotelRepository;
import com.hexa.cozyhavenstay.repository.RoomRepository;
import com.hexa.cozyhavenstay.repository.UserRepository;
import com.hexa.cozyhavenstay.service.BookingService;
import com.hexa.cozyhavenstay.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookedRoomDetailRepository bookedRoomDetailRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository,
                              HotelRepository hotelRepository,
                              RoomRepository roomRepository,
                              BookedRoomDetailRepository bookedRoomDetailRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.bookedRoomDetailRepository = bookedRoomDetailRepository;
    }

    private User getCurrentAuthenticatedUser() {
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + authenticatedUsername));
    }

    @Override
    public BookingResponseDto createBooking(BookingRequestDto requestDto) {
        // --- 1. Validate Input and Fetch Entities ---
        User user = getCurrentAuthenticatedUser();

        Hotel hotel = hotelRepository.findById(requestDto.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + requestDto.getHotelId()));

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
        List<String> bookedRoomNumbersAndTypes = new ArrayList<>(); // For response DTO

        for (Integer roomId : new HashSet<>(requestDto.getRoomIds())) {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

            if (!room.getHotel().getHotelId().equals(hotel.getHotelId())) {
                throw new IllegalArgumentException("Room " + roomId + " does not belong to Hotel " + hotel.getHotelName());
            }

            boolean isRoomBooked = bookedRoomDetailRepository.isRoomBookedForDateRange(
                        roomId, requestDto.getCheckInDate(), requestDto.getCheckOutDate());

            if (isRoomBooked) {
                throw new IllegalStateException("Room " + room.getRoomNumber() + " (" + room.getRoomType().getTypeName() +
                                                ") is already booked for the selected dates.");
            }

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

            bookedRoomNumbersAndTypes.add("Room " + room.getRoomNumber() + " (" + room.getRoomType().getTypeName() + ")");
        }

        // --- 3. Create and Persist Booking ---
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setHotel(hotel);
        booking.setCheckInDate(requestDto.getCheckInDate());
        booking.setCheckOutDate(requestDto.getCheckOutDate());
        booking.setNumberOfAdults(requestDto.getNumberOfAdults());
        booking.setNumberOfChildren(requestDto.getNumberOfChildren());
        booking.setTotalFare(totalBookingFare);
        // bookingDate, createdAt, updatedAt set by @PrePersist
        booking.setPaymentIdReference("TEMP_" + System.currentTimeMillis() + "_" + user.getUserId());
        booking.setBookingStatus("CONFIRMED");
        // No need to set refundStatus here, it's handled by @PrePersist to "N/A"

        booking.setBookedRoomDetails(newBookedRoomDetails);
        newBookedRoomDetails.forEach(detail -> detail.setBooking(booking));

        Booking savedBooking = bookingRepository.save(booking);

        // --- 4. Prepare and Return Response DTO (Using Manual Mapper) ---
        return convertToBookingResponseDto(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> getBookingById(Integer bookingId) {
        return bookingRepository.findById(bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByUserId(Integer userId) {
        return bookingRepository.findByUserUserId(userId);
    }

    @Override
    @Transactional
    public BookingResponseDto cancelBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if ("CANCELLED".equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Booking " + bookingId + " is already cancelled.");
        }
        if (booking.getCheckInDate().isBefore(LocalDate.now().plusDays(2))) {
            throw new IllegalStateException("Booking " + bookingId + " cannot be cancelled less than 2 days before check-in.");
        }

        booking.setBookingStatus("CANCELLED");
        booking.setCancellationDate(LocalDateTime.now());
        booking.setRefundAmount(booking.getTotalFare()); // Assuming full refund for simplicity
        booking.setRefundStatus("PENDING"); // Set refund status to PENDING upon cancellation
        Booking cancelledBooking = bookingRepository.save(booking);

        return convertToBookingResponseDto(cancelledBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingHistoryForUser(Integer userId) {
        List<Booking> bookings = bookingRepository.findByUserUserIdOrderByBookingDateDesc(userId);
        return bookings.stream()
                .map(this::convertToBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllBookingsDetails() {
        List<Booking> allBookings = bookingRepository.findAll();
        return allBookings.stream()
                .map(this::convertToBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingDetailsById(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));
        return convertToBookingResponseDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getUpcomingBookingsForUser(Integer userId) {
        List<Booking> upcomingBookings = bookingRepository.findUpcomingBookingsForUser(userId, LocalDate.now());
        return upcomingBookings.stream()
                .map(this::convertToBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getPastBookingsForUser(Integer userId) {
        List<Booking> pastBookings = bookingRepository.findPastBookingsForUser(userId, LocalDate.now());
        return pastBookings.stream()
                .map(this::convertToBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> listBookingsForHotelOwner(Integer hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel not found with ID: " + hotelId);
        }

        List<Booking> bookings = bookingRepository.findByHotel_HotelId(hotelId);

        return bookings.stream()
                .map(this::convertToBookingResponseDto)
                .collect(Collectors.toList());
    }

    // --- NEW APPROVE REFUND METHOD ---
    @Override
    @Transactional
    public BookingResponseDto approveRefund(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (!"CANCELLED".equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Refund can only be approved for cancelled bookings. Current booking status: " + booking.getBookingStatus());
        }
        if ("APPROVED".equals(booking.getRefundStatus())) {
            throw new IllegalStateException("Refund for booking " + bookingId + " is already approved.");
        }
        if (!"PENDING".equals(booking.getRefundStatus())) {
            // This case handles "N/A" or "REJECTED" refunds
            throw new IllegalStateException("Refund status for booking " + bookingId + " is not 'PENDING'. Current status: " + booking.getRefundStatus());
        }

        booking.setRefundStatus("APPROVED");
        // Optionally, you might want to record who approved and when
        // booking.setRefundApprovalDate(LocalDateTime.now());
        // booking.setRefundApprovedBy(getCurrentAuthenticatedUser().getUserId()); // Requires storing approver ID in Booking entity

        Booking updatedBooking = bookingRepository.save(booking);
        return convertToBookingResponseDto(updatedBooking);
    }
    // ----------------------------------

    // --- MANUAL MAPPING METHOD (UPDATED) ---
    private BookingResponseDto convertToBookingResponseDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setBookingId(booking.getBookingId());
        dto.setHotelName(booking.getHotel() != null ? booking.getHotel().getHotelName() : null);
        dto.setUserName(booking.getUser() != null ? (booking.getUser().getFirstName() + " " + booking.getUser().getLastName()) : null);
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setTotalFare(booking.getTotalFare());
        dto.setBookingStatus(booking.getBookingStatus());
        dto.setBookingDate(booking.getBookingDate());
        // Map new field
        dto.setRefundStatus(booking.getRefundStatus()); // Map refund status

        if (booking.getBookedRoomDetails() != null && !booking.getBookedRoomDetails().isEmpty()) {
            dto.setBookedRoomNumbersAndTypes(
                booking.getBookedRoomDetails().stream()
                    .map(detail -> {
                        String roomNum = (detail.getRoom() != null) ? String.valueOf(detail.getRoom().getRoomNumber()) : "N/A";
                        String roomType = (detail.getRoom() != null && detail.getRoom().getRoomType() != null) ? detail.getRoom().getRoomType().getTypeName() : "N/A";
                        return "Room " + roomNum + " (" + roomType + ")";
                    })
                    .collect(Collectors.toList())
            );
        } else {
            dto.setBookedRoomNumbersAndTypes(new ArrayList<>());
        }
        return dto;
    }
}