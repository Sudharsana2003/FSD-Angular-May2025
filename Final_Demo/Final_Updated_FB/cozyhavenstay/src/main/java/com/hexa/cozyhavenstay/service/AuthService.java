package com.hexa.cozyhavenstay.service;

import com.hexa.cozyhavenstay.dto.LoginRequestDto;
import com.hexa.cozyhavenstay.dto.LoginResponseDto;
import com.hexa.cozyhavenstay.dto.ForgotPasswordRequest;
import com.hexa.cozyhavenstay.dto.ResetPasswordRequest;
import com.hexa.cozyhavenstay.dto.UserDto;

import com.hexa.cozyhavenstay.model.User;
import com.hexa.cozyhavenstay.model.PasswordResetToken;
import com.hexa.cozyhavenstay.model.RoleType;
import com.hexa.cozyhavenstay.repository.UserRepository;
import com.hexa.cozyhavenstay.repository.PasswordResetTokenRepository;

import com.hexa.cozyhavenstay.exception.ResourceNotFoundException;
import com.hexa.cozyhavenstay.exception.DuplicateEntryException; // Added for consistency

import com.hexa.cozyhavenstay.util.JwtUtil; // Your JWT utility

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder; // Keep this, even if userService handles encoding
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional // Good default for service methods that involve database operations
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder; // Injected for password reset encoding
    private final UserService userService; // Injected for user registration delegation

    @Value("${frontend.reset.password.url:http://localhost:4200/reset-password}")
    private String frontendResetPasswordUrl;

    @Autowired // Constructor injection is generally preferred
    public AuthService(AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       UserRepository userRepository,
                       EmailService emailService,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    /**
     * Authenticates a user and generates a JWT token upon successful login.
     * Also updates the user's last login date.
     * @param loginRequest DTO containing username and password.
     * @return LoginResponseDto containing JWT token, user details, and a message.
     * @throws org.springframework.security.core.AuthenticationException if authentication fails.
     * @throws ResourceNotFoundException if user is not found after authentication (should not happen).
     */
    public LoginResponseDto authenticateUser(LoginRequestDto loginRequest) {
        // Authenticate the user credentials.
        // This process uses the username provided in LoginRequestDto.getUsername()
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(), loginRequest.getPassword()) // Uses username for authentication
        );

        // Retrieve UserDetails from the authenticated principal.
        // userDetails.getUsername() here will return the username used for authentication (e.g., "guest123").
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Fetch the full User entity from the database using the USERNAME.
        // THIS IS THE CRITICAL FIX: Changed from findByEmail to findByUsername.
        User user = userRepository.findByUsername(userDetails.getUsername()) // <-- CORRECTED THIS LINE
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found after authentication. This shouldn't happen."));

        // Generate JWT token using the fetched User entity (which should implement UserDetails).
        String jwtToken = jwtUtil.generateToken(user); // Assumes generateToken can take your User entity

        // Update last login date
        user.setLastLoginDate(LocalDateTime.now());
        userRepository.save(user);

        // Prepare the response DTO
        LoginResponseDto response = new LoginResponseDto();
        response.setJwtToken(jwtToken);
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername()); // Send back the actual username
        response.setUserType(user.getRole().name()); // Assuming getRole() returns RoleType
        response.setMessage("Login successful!");

        return response;
    }

    /**
     * Handles forgot password request. Generates a password reset token,
     * stores it in the database, and sends a reset link via email.
     * @param email The email of the user requesting password reset.
     * @throws ResourceNotFoundException if the user with the given email is not found.
     */
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with email: " + email));

        // --- START OF MODIFIED LOGIC ---
        // Try to find an existing token for this user
        PasswordResetToken existingToken = passwordResetTokenRepository.findByUser(user)
                                                                       .orElse(null); // Use orElse(null) to handle Optional

        String newTokenString = UUID.randomUUID().toString(); // Always generate a new token string
        LocalDateTime newExpiryDate = LocalDateTime.now().plusHours(1); // New expiry for the token

        if (existingToken != null) {
            // If a token already exists, update its details
            existingToken.setToken(newTokenString);
            existingToken.setExpiryDate(newExpiryDate);
            existingToken.setUsed(false); // Make sure it's marked as unused for the new request
            passwordResetTokenRepository.save(existingToken); // This will perform an UPDATE
        } else {
            // If no token exists, create a brand new one
            PasswordResetToken newResetToken = new PasswordResetToken(newTokenString, user, newExpiryDate);
            passwordResetTokenRepository.save(newResetToken); // This will perform an INSERT
        }
        // --- END OF MODIFIED LOGIC ---

        // Construct the reset link for the frontend using the newly generated token string
        String resetLink = frontendResetPasswordUrl + "?token=" + newTokenString; // Use newTokenString for the link

        // Prepare and send the email
        String subject = "Cozy Haven Stay - Password Reset Request";
        String htmlBody = "<html><body>"
                + "<p>Dear " + user.getFirstName() + ",</p>"
                + "<p>You have requested to reset your password for your Cozy Haven Stay account.</p>"
                + "<p>Please click on the link below to reset your password:</p>"
                + "<p><a href=\"" + resetLink + "\" target=\"_blank\">Reset Password</a></p>"
                + "<p><b>This link will expire in 1 hour.</b> If you did not request a password reset, please ignore this email.</p>"
                + "<p>Regards,<br/>Cozy Haven Stay Support</p>"
                + "</body></html>";

        System.out.println("DEBUG: Generated resetLink for " + user.getEmail() + ": '" + resetLink + "'");
        emailService.sendHtmlEmail(user.getEmail(), subject, htmlBody);
    }

    /**
     * Resets the user's password using a valid token.
     * Marks the token as used after a successful reset.
     * @param token The password reset token.
     * @param newPassword The new password for the user.
     * @throws ResourceNotFoundException if the token is invalid or not found.
     * @throws IllegalArgumentException if the token is expired or already used.
     */
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invalid or expired password reset token."));

        if (resetToken.isExpired() || resetToken.isUsed()) {
            throw new IllegalArgumentException("Password reset token has expired or already been used.");
        }

        User user = resetToken.getUser();
        if (user == null) {
            throw new ResourceNotFoundException("User associated with password reset token not found.");
        }

        // 1. Encode and update the user's password
        user.setPassword(passwordEncoder.encode(newPassword)); // Assuming your User model has setPassword for password_hash
        userRepository.save(user);

        // 2. Mark the token as used to prevent reuse
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // 3. Send password reset confirmation email
        sendPasswordResetConfirmationEmail(user);
    }

    /**
     * Sends a confirmation email to the user after a successful password reset.
     * @param user The user whose password was reset.
     */
    private void sendPasswordResetConfirmationEmail(User user) {
        String subject = "Cozy Haven Stay - Password Successfully Reset";
        String htmlBody = "<html><body>"
                + "<p>Dear " + user.getFirstName() + ",</p>"
                + "<p>Your password for your Cozy Haven Stay account has been successfully reset.</p>"
                + "<p>If you did not initiate this change, please contact support immediately.</p>"
                + "<p>Regards,<br/>Cozy Haven Stay Support</p>"
                + "</body></html>";

        emailService.sendHtmlEmail(user.getEmail(), subject, htmlBody);
    }

    /**
     * Registers a new user with the GUEST role by delegating to UserService.
     * @param userDto DTO containing user registration details.
     */
    public void registerGuest(UserDto userDto) {
        userDto.setUserType("GUEST"); // Ensure this is set, even if controller does.
        userService.registerUser(userDto); // Delegates to UserService
    }

    /**
     * Registers a new user with the HOTEL_OWNER role by delegating to UserService.
     * This method is expected to be called only when the caller has ADMIN privileges (enforced by SecurityConfig).
     * @param userDto DTO containing user registration details.
     */
    public void registerHotelOwner(UserDto userDto) {
        userDto.setUserType("HOTEL_OWNER");
        userService.registerUser(userDto);
    }

    /**
     * Registers a new user with the ADMIN role by delegating to UserService.
     * This method is expected to be called only when the caller has ADMIN privileges (enforced by SecurityConfig).
     * This prevents unauthorized creation of admin accounts.
     * @param userDto DTO containing user registration details.
     */
    public void registerAdmin(UserDto userDto) {
        userDto.setUserType("ADMIN");
        userService.registerUser(userDto);
    }
}