package com.hexa.cozyhavenstay.controller;

import com.hexa.cozyhavenstay.dto.UserDto;
import com.hexa.cozyhavenstay.dto.UserResponseDto;
import com.hexa.cozyhavenstay.service.UserService;
import com.hexa.cozyhavenstay.exception.DuplicateEntryException;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Removed User import as it's not directly used in the controller methods here
// import com.hexa.cozyhavenstay.model.User; 
// NEW IMPORT: For your custom security service
import com.hexa.cozyhavenstay.service.UserSecurityService;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // Autowire your new UserSecurityService
    @Autowired
    private UserSecurityService userSecurityService; // Inject the service here


    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new user. This endpoint is typically for ADMINs to create users,
     * as general public registration is usually handled via /api/auth/register/guest etc.
     * Requires ADMIN role. (NEW! - Missing from table)
     *
     * @param userDto DTO containing user registration details.
     * @return ResponseEntity indicating success or conflict/error.
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMINs can create users via this endpoint
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto userDto) {
        try {
            userService.registerUser(userDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
        } catch (DuplicateEntryException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Global exception handler for validation errors (e.g., @Valid annotations).
     * This can also be in a separate @ControllerAdvice class for broader scope.
     *
     * @param ex The MethodArgumentNotValidException.
     * @return A map of field names to error messages.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((org.springframework.validation.FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    /**
     * Retrieves user details by a specific user ID. (4.1 Get User by ID (Admin))
     * Accessible only by users with the ADMIN role.
     *
     * @param userId The ID of the user to retrieve.
     * @return UserResponseDto containing user details.
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMINs can fetch any user by ID
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Integer userId) {
        UserResponseDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Retrieves the profile details of the currently authenticated user. (2.1 View Own Profile)
     * Accessible by any authenticated user.
     *
     * @param userDetails The authenticated user's details injected by Spring Security.
     * @return UserResponseDto containing the current user's profile information.
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()") // Any authenticated user can access their own profile
    public ResponseEntity<UserResponseDto> getCurrentUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponseDto user = userService.getCurrentUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(user);
    }

    /**
     * Retrieves a list of all registered users in the system. (4.2 Get All Users (Admin))
     * Accessible only by users with the ADMIN role for security reasons.
     *
     * @return A list of UserResponseDto objects.
     */
    @GetMapping // Matches /api/users
    @PreAuthorize("hasRole('ADMIN')") // Only ADMINs can view the list of all users
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        if (users.isEmpty()) { // Add check for empty list for consistency
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(users);
    }

    /**
     * Updates user details for a specific user ID. (2.2 Update Own Profile, 4.3 Update User (Admin))
     * Accessible by:
     * 1. Users with the ADMIN role (can update any user).
     * 2. The user themselves (can update their own profile).
     *
     * @param userId The ID of the user to update.
     * @param userDto DTO containing updated user information.
     * @return UserResponseDto of the updated user.
     */
    @PutMapping("/{userId}")
    // FINAL @PreAuthorize: Uses the custom userSecurity bean to check if the current user is the owner
    // This correctly combines admin override with user-specific ownership check.
    @PreAuthorize("hasRole('ADMIN') or @userSecurityService.isCurrentUser(#userId)") // <-- ADDED 'Service' // NOTE: Assuming @userSecurityService
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Integer userId, @RequestBody UserDto userDto) {
        UserResponseDto updatedUser = userService.updateUser(userId, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deletes a user from the system by their ID. (4.4 Delete User (Admin))
     * Accessible only by users with the ADMIN role for system integrity.
     *
     * @param userId The ID of the user to delete.
     * @return ResponseEntity with no content upon successful deletion.
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMINs can delete users
    public ResponseEntity<Void> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}