package com.hexa.cozyhavenstay.controller;

import com.hexa.cozyhavenstay.dto.LoginRequestDto;
import com.hexa.cozyhavenstay.dto.LoginResponseDto;
import com.hexa.cozyhavenstay.dto.ForgotPasswordRequest;
import com.hexa.cozyhavenstay.dto.ResetPasswordRequest;
import com.hexa.cozyhavenstay.dto.UserDto;
import com.hexa.cozyhavenstay.service.AuthService;
import com.hexa.cozyhavenstay.exception.ResourceNotFoundException;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Handles user login requests. (Table 1.6 Login (Guest))
    // This endpoint handles ALL login types (Guest, Owner, Admin)
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> authenticateUser(@RequestBody LoginRequestDto loginRequest) {
        try {
            LoginResponseDto response = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (org.springframework.security.core.AuthenticationException e) {
            LoginResponseDto errorResponse = new LoginResponseDto(null, null, null, null, "Authentication failed: Invalid email or password.");
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            LoginResponseDto errorResponse = new LoginResponseDto(null, null, null, null, "An error occurred during login: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // NEW: Specific Login for Owner (Table 1.8 Login (Owner))
    // IMPORTANT: Ensure your AuthService.authenticateUser can differentiate or handle logic based on specific endpoint if needed.
    @PostMapping("/login/owner")
    public ResponseEntity<LoginResponseDto> authenticateOwner(@RequestBody LoginRequestDto loginRequest) {
        // You might call the same authenticateUser or a specialized one in AuthService
        // For simplicity, assuming authenticateUser can handle this
        return authenticateUser(loginRequest);
    }

    // NEW: Specific Login for Admin (Table 1.10 Login (Admin))
    // IMPORTANT: Ensure your AuthService.authenticateUser can differentiate or handle logic based on specific endpoint if needed.
    @PostMapping("/login/admin")
    public ResponseEntity<LoginResponseDto> authenticateAdmin(@RequestBody LoginRequestDto loginRequest) {
        // You might call the same authenticateUser or a specialized one in AuthService
        // For simplicity, assuming authenticateUser can handle this
        return authenticateUser(loginRequest);
    }


    // Handles guest user registration. (Table 1.7 Register (Guest))
    @PostMapping("/register/guest")
    public ResponseEntity<String> registerGuest(@Valid @RequestBody UserDto userDto) {
        System.out.println("DEBUG: AuthController received UserDto for guest registration.");
        System.out.println("DEBUG: DTO username in controller: '" + userDto.getUsername() + "'");
        System.out.println("DEBUG: DTO email in controller: '" + userDto.getEmail() + "'");
        System.out.println("DEBUG: DTO countryCode in controller: '" + userDto.getCountryCode() + "'");
        System.out.println("DEBUG: DTO phoneNumber in controller: '" + userDto.getLocalPhoneNumber() + "'");
        try {
            userDto.setUserType("GUEST");
            authService.registerGuest(userDto);
            return new ResponseEntity<>("Guest registered successfully! You can now login.", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred during guest registration: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Handles hotel owner registration. (Table 1.9 Register (Owner))
    @PostMapping("/register/owner") // <-- MODIFIED: Corrected to /register/owner to match table 1.9 precisely.
    public ResponseEntity<String> registerHotelOwner(@Valid @RequestBody UserDto userDto) {
        userDto.setUserType("HOTEL_OWNER"); // Ensure your UserType enum / string is "HOTEL_OWNER"
        try {
            authService.registerHotelOwner(userDto);
            return new ResponseEntity<>("Hotel Owner registered successfully!", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred during hotel owner registration: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Handles admin registration. (NOT in table's public section, remains ADMIN-protected)
    @PostMapping("/register/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> registerAdmin(@Valid @RequestBody UserDto userDto) {
        userDto.setUserType("ADMIN");
        try {
            authService.registerAdmin(userDto);
            return new ResponseEntity<>("Admin registered successfully!", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred during admin registration: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Handles forgot password requests. (Table 1.11 Forgot Password)
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request.getEmail());
            return ResponseEntity.ok("Password reset link sent to your email if the account exists.");
        } catch (ResourceNotFoundException e) {
            System.out.println("Forgot password request for non-existent email: " + request.getEmail());
            return ResponseEntity.ok("Password reset link sent to your email if the account exists.");
        } catch (Exception e) {
            System.err.println("Error during forgot password process for email " + request.getEmail() + ": " + e.getMessage());
            return new ResponseEntity<>("An error occurred while processing your request. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Handles password reset requests. (Table 1.12 Reset Password)
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.getToken(), request.getPassword());
            return ResponseEntity.ok("Password has been reset successfully!");
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Error resetting password for token " + request.getToken() + ": " + e.getMessage());
            return new ResponseEntity<>("An error occurred while resetting your password. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}