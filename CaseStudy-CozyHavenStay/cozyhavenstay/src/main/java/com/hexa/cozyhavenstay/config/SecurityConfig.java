package com.hexa.cozyhavenstay.config;

import com.hexa.cozyhavenstay.service.CustomUserDetailsService;
import com.hexa.cozyhavenstay.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Ensures @PreAuthorize works
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authorize -> authorize
                // --- Public (Browse & Auth) Endpoints - Based EXACTLY on your table Section 1 (1.1 to 1.12) ---

                // Authentication related (Table 1.6, 1.7, 1.8, 1.9, 1.10, 1.11, 1.12)
                .requestMatchers(HttpMethod.POST,
                    "/api/auth/login",            // Table 1.6
                    "/api/auth/register/guest",   // Table 1.7
                    "/api/auth/login/owner",      // Table 1.8
                    "/api/auth/register/owner",   // Table 1.9
                    "/api/auth/login/admin",      // Table 1.10
                    "/api/auth/forgot-password",  // Table 1.11
                    "/api/auth/reset-password"    // Table 1.12
                ).permitAll()

                // Browse/View related (Table 1.1, 1.2, 1.3, 1.4, 1.5)
                .requestMatchers(HttpMethod.GET,
                    "/api/hotels",                   // Table 1.1
                    "/api/hotels/{hotelId}",         // Table 1.2
                    "/api/hotels/{hotelId}/rooms",   // Table 1.3 (Matches table, implies RoomController change)
                    "/api/rooms/{roomId}",           // Table 1.4 (Matches table, implies RoomController change)
                    "/api/amenities"                 // Table 1.5
                ).permitAll()

                // Register Admin is NOT in your public table, so it remains protected.
                .requestMatchers(HttpMethod.POST, "/api/auth/register/admin").hasRole("ADMIN")

                // All other requests not explicitly permitted above require authentication.
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Authentication required.")
                )
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden: You do not have sufficient permissions to access this resource.")
                )
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}