// src/main/java/com/hexa/cozyhavenstay/filter/JwtAuthenticationFilter.java
package com.hexa.cozyhavenstay.filter;

import com.hexa.cozyhavenstay.util.JwtUtil; // NEW IMPORT
import com.hexa.cozyhavenstay.service.CustomUserDetailsService; // NEW IMPORT
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

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil; // Inject our JwtUtil
    
    @Autowired
    private CustomUserDetailsService customUserDetailsService; // Inject our UserDetailsService

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // Extract JWT from Authorization header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // "Bearer " is 7 characters
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // Handle token extraction/parsing errors (e.g., malformed token, signature issues)
                System.err.println("Error extracting username from token: " + e.getMessage());
                // You might want to send a specific error response here instead of just logging
            }
        }

        // If username is extracted and no authentication is currently set in context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load UserDetails from username
            UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);

            // Validate token against UserDetails
            if (jwtUtil.validateToken(jwt, userDetails)) {
                // If token is valid, create an authentication object
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                
                // Set authentication details (remote IP address, session ID)
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Set the authentication object in the SecurityContext
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            } else {
                System.err.println("JWT Token validation failed for user: " + username);
            }
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}