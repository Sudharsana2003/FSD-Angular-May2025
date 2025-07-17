package com.hexa.cozyhavenstay.service;

import com.hexa.cozyhavenstay.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

// CHANGE THIS LINE:
@Service("userSecurityService") // <-- The name must EXACTLY match what @PreAuthorize uses
public class UserSecurityService {

    /**
     * Checks if the currently authenticated user is the same as the user with the given ID.
     * This method is designed to be called from @PreAuthorize expressions.
     *
     * @param userId The ID of the user being accessed/modified.
     * @return true if the authenticated user's ID matches the provided userId, false otherwise.
     */
    public boolean isCurrentUser(Integer userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false; // No authenticated user
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            User currentUser = (User) principal;
            return currentUser.getUserId().equals(userId);
        }
        return false;
    }
}