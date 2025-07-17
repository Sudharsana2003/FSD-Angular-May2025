// src/main/java/com/hexa/cozyhavenstay/filter/JwtAuthenticationFilter.java
package com.hexa.cozyhavenstay.filter;

import com.hexa.cozyhavenstay.util.JwtUtil;
import com.hexa.cozyhavenstay.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// For logging (recommended over System.err.println for production)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        logger.debug("--- JwtAuthenticationFilter: Processing request for URL: {} ---", request.getRequestURI());

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 1. Extract JWT from Authorization header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // "Bearer " is 7 characters
            logger.debug("Authorization header found. Extracted JWT: {}", jwt);
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.debug("Username extracted from JWT: {}", username);
            } catch (Exception e) {
                logger.warn("Error extracting username from token (Token might be invalid/expired): {}",
                        e.getMessage());
                // Consider setting HTTP status 401 here if token is clearly invalid
            }
        } else {
            logger.debug("No Bearer token found in Authorization header for URL: {}", request.getRequestURI());
        }

        // 2. If username is extracted and no authentication is currently set in context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("Username is present and SecurityContext is empty. Attempting to authenticate...");

            // 3. Load UserDetails from username
            UserDetails userDetails = null;
            try {
                userDetails = this.customUserDetailsService.loadUserByUsername(username);
                logger.debug("UserDetails loaded for username: {}. Authorities: {}", username,
                        userDetails.getAuthorities());
            } catch (Exception e) {
                logger.warn("Failed to load UserDetails for username {}: {}", username, e.getMessage());
                // This could happen if username from token doesn't exist in DB
            }

            if (userDetails != null) {
                // 4. Validate token against UserDetails
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    logger.debug("JWT Token is VALID for user: {}", username);
                    // If token is valid, create an authentication object
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    // Set authentication details (remote IP address, session ID)
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set the authentication object in the SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    logger.debug("SecurityContextHolder populated for user: {}", username);
                } else {
                    logger.warn("JWT Token validation FAILED for user: {}. Token: {}", username, jwt);
                    // This could mean token expired, signature mismatch, etc.
                }
            } else {
                logger.warn("UserDetails is null for username: {}. Cannot validate token.", username);
            }
        } else {
            if (username == null) {
                logger.debug("No username extracted from token for URL: {}", request.getRequestURI());
            }
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                logger.debug("SecurityContext already contains authentication for user: {}",
                        SecurityContextHolder.getContext().getAuthentication().getName());
            }
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
        logger.debug("--- JwtAuthenticationFilter: Finished processing request for URL: {} ---",
                request.getRequestURI());
    }
}
