package com.hexaware.tasks.app_root.controller;


import com.hexaware.tasks.app_root.auth.LoginRequest;
import com.hexaware.tasks.app_root.auth.LoginResponse;
import com.hexaware.tasks.app_root.auth.RegisterRequest;
import com.hexaware.tasks.app_root.entity.User; // CORRECTED: Removed double dot
import com.hexaware.tasks.app_root.repository.UserRepository;
import com.hexaware.tasks.app_root.service.CustomUserDetailsService;
import com.hexaware.tasks.app_root.util.JwtUtil; // Corrected import path

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService; // Use CustomUserDetailsService
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository; // Inject UserRepository for registration
    private final PasswordEncoder passwordEncoder; // Inject PasswordEncoder for hashing passwords

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager,
                                    CustomUserDetailsService customUserDetailsService, // Change parameter type
                                    JwtUtil jwtUtil,
                                    UserRepository userRepository,
                                    PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword())); // Hash password before saving
        user.setRoles(registerRequest.getRoles() != null ? registerRequest.getRoles() : "ROLE_USER"); // Default role

        userRepository.save(user); // Save user to database

        return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> createAuthenticationToken(@RequestBody LoginRequest authenticationRequest) throws Exception {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );

            // Load UserDetails via our CustomUserDetailsService (which fetches from DB)
            final UserDetails userDetails = customUserDetailsService.loadUserByUsername(authenticationRequest.getUsername());
            final String jwt = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(new LoginResponse(jwt));

        } catch (Exception e) {
            throw new Exception("Incorrect username or password", e);
        }
    }
}