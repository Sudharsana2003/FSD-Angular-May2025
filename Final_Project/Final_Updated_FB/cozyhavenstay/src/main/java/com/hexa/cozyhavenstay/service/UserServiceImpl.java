package com.hexa.cozyhavenstay.service;

import com.hexa.cozyhavenstay.dto.UserDto;
import com.hexa.cozyhavenstay.dto.UserResponseDto;
import com.hexa.cozyhavenstay.model.User;
import com.hexa.cozyhavenstay.model.RoleType;
import com.hexa.cozyhavenstay.repository.UserRepository;
import com.hexa.cozyhavenstay.exception.ResourceNotFoundException;
import com.hexa.cozyhavenstay.exception.DuplicateEntryException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @Override
    public UserResponseDto registerUser(UserDto userDto) {
        // --- DEBUG PRINTS START ---
        System.out.println("DEBUG: Entering registerUser method in UserServiceImpl.");
        System.out.println("DEBUG: UserDto username: '" + userDto.getUsername() + "'");
        System.out.println("DEBUG: UserDto email: '" + userDto.getEmail() + "'");
        System.out.println("DEBUG: UserDto firstName: '" + userDto.getFirstName() + "'");
        System.out.println("DEBUG: UserDto lastName: '" + userDto.getLastName() + "'");
        System.out.println("DEBUG: UserDto gender: '" + userDto.getGender() + "'");
        System.out.println("DEBUG: UserDto countryCode received: '" + userDto.getCountryCode() + "'");
        System.out.println("DEBUG: UserDto localPhoneNumber received: '" + userDto.getLocalPhoneNumber() + "'");
        System.out.println("DEBUG: UserDto address: '" + userDto.getAddress() + "'");
        System.out.println("DEBUG: UserDto userType (input from DTO): '" + userDto.getUserType() + "'");
        // --- DEBUG PRINTS END ---

        // Uniqueness checks
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new DuplicateEntryException("Username '" + userDto.getUsername() + "' already exists.");
        }
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new DuplicateEntryException("Email '" + userDto.getEmail() + "' already registered.");
        }
        if (userRepository.findByLocalPhoneNumber(userDto.getLocalPhoneNumber()).isPresent()) {
            throw new DuplicateEntryException("Local phone number '" + userDto.getLocalPhoneNumber() + "' already registered.");
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setGender(userDto.getGender());
        user.setCountryCode(userDto.getCountryCode());
        user.setLocalPhoneNumber(userDto.getLocalPhoneNumber());
        user.setAddress(userDto.getAddress());

        // --- START OF THE CRITICAL UPDATE ---
        // Dynamically set the role based on the userType from the DTO
        try {
            // Convert the string userType from DTO to the RoleType enum
            user.setRole(RoleType.valueOf(userDto.getUserType().toUpperCase()));
            System.out.println("DEBUG: Role set on entity (after conversion from DTO): '" + user.getRole().name() + "'");
        } catch (IllegalArgumentException e) {
            // If the userType string from DTO doesn't match a RoleType enum, throw an error
            throw new IllegalArgumentException("Invalid user type provided in registration request: " + userDto.getUserType() + ". Valid types are GUEST, HOTEL_OWNER, ADMIN.");
        }
        // --- END OF THE CRITICAL UPDATE ---

        user.setIsActive(true);
        user.setRegistrationDate(LocalDateTime.now());

        // Encode password before saving
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        User savedUser = userRepository.save(user);
        System.out.println("DEBUG: User saved successfully with ID: " + savedUser.getUserId() + " and final Role: " + savedUser.getRole().name());
        return convertToDto(savedUser);
    }

    @Override
    public UserResponseDto getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                                 .filter(User::getIsActive)
                                 .orElseThrow(() -> new ResourceNotFoundException("Active User not found with ID: " + userId));
        return convertToDto(user);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findByIsActiveTrue();
        return users.stream()
                     .map(this::convertToDto)
                     .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto updateUser(Integer userId, UserDto userDto) {
        User existingUser = userRepository.findById(userId)
                                         .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        boolean passwordChanged = false;

        // Update username if provided and different, check for duplicates
        if (userDto.getUsername() != null && !userDto.getUsername().equals(existingUser.getUsername())) {
            Optional<User> userWithSameUsername = userRepository.findByUsername(userDto.getUsername());
            if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getUserId().equals(userId)) {
                throw new DuplicateEntryException("Username '" + userDto.getUsername() + "' is already taken by another user.");
            }
            existingUser.setUsername(userDto.getUsername());
        }

        // Update email if provided and different, check for duplicates
        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            Optional<User> userWithSameEmail = userRepository.findByEmail(userDto.getEmail());
            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getUserId().equals(userId)) {
                throw new DuplicateEntryException("Email '" + userDto.getEmail() + "' is already registered by another user.");
            }
            existingUser.setEmail(userDto.getEmail());
        }

        // Update local phone number if provided and different, check for duplicates
        if (userDto.getLocalPhoneNumber() != null && !userDto.getLocalPhoneNumber().equals(existingUser.getLocalPhoneNumber())) {
            Optional<User> userWithSamePhoneNumber = userRepository.findByLocalPhoneNumber(userDto.getLocalPhoneNumber());
            if (userWithSamePhoneNumber.isPresent() && !userWithSamePhoneNumber.get().getUserId().equals(userId)) {
                throw new DuplicateEntryException("Local phone number '" + userDto.getLocalPhoneNumber() + "' is already registered by another user.");
            }
            existingUser.setLocalPhoneNumber(userDto.getLocalPhoneNumber());
        }

        // Update other non-unique fields if provided
        if (userDto.getFirstName() != null) existingUser.setFirstName(userDto.getFirstName());
        if (userDto.getLastName() != null) existingUser.setLastName(userDto.getLastName());
        if (userDto.getGender() != null) existingUser.setGender(userDto.getGender());
        if (userDto.getCountryCode() != null) existingUser.setCountryCode(userDto.getCountryCode());
        if (userDto.getAddress() != null) existingUser.setAddress(userDto.getAddress());

        // Update userType (role) if provided, converting String to RoleType enum
        // This is for updating existing users, not new registrations
        if (userDto.getUserType() != null) {
            try {
                existingUser.setRole(RoleType.valueOf(userDto.getUserType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid user type provided: " + userDto.getUserType() + ". Valid types are GUEST, HOTEL_OWNER, ADMIN.");
            }
        }
        // Update isActive status if provided
        if (userDto.getIsActive() != null) existingUser.setIsActive(userDto.getIsActive());

        // Update password if provided
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
            passwordChanged = true;
        }

        User updatedUser = userRepository.save(existingUser);

        // Send password change confirmation email if password was updated
        if (passwordChanged) {
            sendPasswordChangeConfirmationEmail(updatedUser.getEmail(), updatedUser.getUsername());
        }

        return convertToDto(updatedUser);
    }

    @Override
    public boolean deleteUser(Integer userId) {
        User user = userRepository.findById(userId)
                                 .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setIsActive(false);
        userRepository.save(user);
        return true;
    }

    @Override
    public UserResponseDto getCurrentUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                                 .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found."));
        return convertToDto(user);
    }

    // Helper method to convert User entity to UserResponseDto
    private UserResponseDto convertToDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        // For full_name, ensure your User entity has an appropriate getter or derived property
        dto.setFullName(user.getFullName() != null ? user.getFullName() : user.getFirstName() + " " + user.getLastName());
        dto.setGender(user.getGender());
        dto.setCountryCode(user.getCountryCode());
        dto.setLocalPhoneNumber(user.getLocalPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setUserType(user.getRole().name()); // Make sure this line correctly uses getRole() to get the enum name
        dto.setRegistrationDate(user.getRegistrationDate());
        dto.setLastLoginDate(user.getLastLoginDate());
        dto.setIsActive(user.getIsActive());
        return dto;
    }

    private void sendPasswordChangeConfirmationEmail(String recipientEmail, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipientEmail);
            helper.setSubject("Password Changed Successfully for CozyHavenStay");

            String htmlContent = "<html><body>"
                    + "<p>Dear " + username + ",</p>"
                    + "<p>Your password for Cozy Haven Stay has been successfully changed on "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + ".</p>"
                    + "<p>If you did not make this change, please contact support immediately.</p>"
                    + "<p>Thank you,<br/>The Cozy Haven Stay Team</p>"
                    + "</body></html>";
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Password change confirmation email sent to: " + recipientEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send password change confirmation email to " + recipientEmail + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while sending password change email to " + recipientEmail + ": " + e.getMessage());
        }
    }
}