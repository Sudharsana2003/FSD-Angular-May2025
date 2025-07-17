// package com.hexa.cozyhavenstay.service;

package com.hexa.cozyhavenstay.service;

import com.hexa.cozyhavenstay.dto.BookingResponseDto;
import com.hexa.cozyhavenstay.exception.ResourceNotFoundException;
import com.hexa.cozyhavenstay.model.Booking;
import com.hexa.cozyhavenstay.model.User;
import com.hexa.cozyhavenstay.model.UserWallet;
import com.hexa.cozyhavenstay.model.Payment; // ⭐ NEW
import com.hexa.cozyhavenstay.repository.BookingRepository;
import com.hexa.cozyhavenstay.repository.UserRepository;
import com.hexa.cozyhavenstay.repository.UserWalletRepository;
import com.hexa.cozyhavenstay.repository.PaymentRepository; // ⭐ NEW

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant; // For paymentDate
import java.util.UUID; // For generating a unique payment ID

@Service
public class WalletService {

    private final UserWalletRepository userWalletRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository; // ⭐ NEW

    public WalletService(UserWalletRepository userWalletRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository,
            PaymentRepository paymentRepository) { // ⭐ NEW parameter
        this.userWalletRepository = userWalletRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository; // ⭐ NEW assignment
    }

    @Transactional
    public void initializeUserWallet(Integer userId) {
        userWalletRepository.findByUserId(userId).ifPresent(wallet -> {
            throw new IllegalStateException("Wallet already exists for user ID: " + userId);
        });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        UserWallet newUserWallet = new UserWallet(user);
        userWalletRepository.save(newUserWallet);
    }

    @Transactional(readOnly = true)
    public double getUserWalletBalance(Integer userId) {
        UserWallet userWallet = userWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user ID: " + userId));
        return userWallet.getBalance().doubleValue();
    }

    @Transactional
    public double addFundsToUserWallet(Integer userId, Double amount) {
        if (amount == null || amount.compareTo(0.0) <= 0) {
            throw new IllegalArgumentException("Amount to add must be positive.");
        }

        UserWallet userWallet = userWalletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user ID: " + userId));

        BigDecimal amountToAdd = BigDecimal.valueOf(amount);
        userWallet.setBalance(userWallet.getBalance().add(amountToAdd));
        userWalletRepository.save(userWallet);
        // Optionally, create a 'CREDIT' payment record here
        // For simplicity, we're not doing that in this "very simple" setup for
        // add-funds
        // but for a robust system, you would.
        return userWallet.getBalance().doubleValue();
    }

    @Transactional
    public BookingResponseDto payForBookingWithWallet(Integer userId, Integer bookingId, Double totalFare) {
        // 1. Fetch wallet with pessimistic lock
        UserWallet userWallet = userWalletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user ID: " + userId));

        // 2. Fetch booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        // 3. Validate booking state and ownership
        if (!booking.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Booking does not belong to the specified user.");
        }
        // Check if booking is already CONFIRMED or has a successful payment
        if ("CONFIRMED".equalsIgnoreCase(booking.getBookingStatus()) ||
                "PAID".equalsIgnoreCase(booking.getBookingStatus())) {
            throw new IllegalStateException("Booking has already been paid or confirmed.");
        }
        // Validate totalFare against booking's actual totalFare
        if (booking.getTotalFare() == null || BigDecimal.valueOf(totalFare).compareTo(booking.getTotalFare()) != 0) {
            throw new IllegalArgumentException(
                    "Total fare mismatch. Expected: " + booking.getTotalFare() + ", Received: " + totalFare);
        }

        // 4. Check balance
        BigDecimal fareBigDecimal = BigDecimal.valueOf(totalFare);
        if (userWallet.getBalance().compareTo(fareBigDecimal) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient wallet balance. Current: " + userWallet.getBalance() + ", Required: " + totalFare);
        }

        // 5. Deduct funds from wallet
        userWallet.setBalance(userWallet.getBalance().subtract(fareBigDecimal));
        userWalletRepository.save(userWallet); // Persist updated wallet balance

        // 6. Create a Payment record
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(fareBigDecimal);
        payment.setPaymentMethod("WALLET");
        payment.setPaymentStatus("SUCCESS"); // Assuming immediate success
        payment.setTransactionType("DEBIT");
        // Generate a unique reference ID for this payment transaction
        payment.setPaymentReferenceId(
                "WALLET_TXN_" + Instant.now().toEpochMilli() + "_" + UUID.randomUUID().toString().substring(0, 8));

        paymentRepository.save(payment); // Save the new payment record

        // 7. Update booking status
        booking.setBookingStatus("CONFIRMED"); // Or "PAID", then transition to CONFIRMED
        // The payment details are now linked via the 'payments' Set in Booking
        // You do NOT set paymentId, paymentDate directly on Booking anymore.
        bookingRepository.save(booking); // Persist updated booking status

        // 8. Return booking response DTO
        return convertToBookingResponseDto(booking);
    }

    // Helper method to convert Booking entity to BookingResponseDto
    private BookingResponseDto convertToBookingResponseDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setBookingId(booking.getBookingId());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        // Use getTotalGuests() from Booking entity
        dto.setNumberOfAdults(booking.getNumberOfAdults()); // Assuming these exist now
        dto.setNumberOfChildren(booking.getNumberOfChildren()); // Assuming these exist now

        dto.setTotalFare(booking.getTotalFare()); // Correct: passes BigDecimal
        dto.setBookingStatus(booking.getBookingStatus());
        dto.setBookingDate(booking.getBookingDate());

        // Extract Hotel Name and User Name
        if (booking.getHotel() != null) {
            dto.setHotelId(booking.getHotel().getHotelId());
            dto.setHotelName(booking.getHotel().getHotelName());
        }
        if (booking.getUser() != null) {
            dto.setUserName(booking.getUser().getUsername()); // Assuming username is what you want
        }

        // For refundStatus, you'd typically check the latest payment record for refund
        // status
        // For simplicity, if your BookingResponseDto still needs a refundStatus field,
        // you might pull it from the most recent refund-related payment, or rely on
        // Booking's own field
        // if you re-add it for booking-level status.
        // For now, let's assume it's related to the 'refunds' field in your Booking
        // DTO.
        // If your Booking entity still has `refundStatus` as a direct field, then:
        // dto.setRefundStatus(booking.getRefundStatus());
        // Otherwise, this DTO field might need to be removed or derived from Payment
        // entities.

        // To populate bookedRoomNumbersAndTypes, you'd iterate through
        // booking.getBookedRoomDetails()
        // and extract the relevant information. This might require injecting
        // RoomService or similar.
        // For now, I'll leave it as a placeholder or you can implement it based on your
        // BookedRoomDetail structure.
        // Example (assuming BookedRoomDetail has getRoom().getRoomNumber() and
        // getRoom().getRoomType() methods):
        // List<String> roomDetails = booking.getBookedRoomDetails().stream()
        // .map(brd -> brd.getRoom().getRoomNumber() + " (" +
        // brd.getRoom().getRoomType() + ")")
        // .collect(Collectors.toList());
        // dto.setBookedRoomNumbersAndTypes(roomDetails);
        return dto;
    }
}