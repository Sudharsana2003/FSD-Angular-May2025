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
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
                        throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for API requests (stateless)
                                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Configure CORS
                                .authorizeHttpRequests(authorize -> authorize
                                                // --- Public (Browse & Auth) Endpoints ---
                                                // Authentication related (POST requests for login, register, password
                                                // management)
                                                .requestMatchers(HttpMethod.POST,
                                                                "/api/auth/login",
                                                                "/api/auth/register/guest",
                                                                "/api/auth/login/owner",
                                                                "/api/auth/register/owner",
                                                                "/api/auth/forgot-password",
                                                                "/api/auth/reset-password")
                                                .permitAll()

                                                // Browse/View related (GET requests for public data)
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/hotels",
                                                                "/api/hotels/*",
                                                                "/api/hotels/*/rooms",
                                                                "/api/hotels/*/reviews",
                                                                "/api/rooms/*",
                                                                "/api/amenities")
                                                .permitAll()

                                                // --- Role-Based Endpoints (CRITICAL: Place more specific/restrictive
                                                // rules FIRST) ---

                                                // Admin-specific endpoints: All paths under /api/admin/** require ADMIN
                                                // role
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                                                // Specific endpoint for admin registration (if it's not under
                                                // /api/admin already)
                                                // If "/api/auth/register/admin" is the exact path, it needs its own
                                                // specific rule.
                                                // If you move it under /api/admin/, then the "/api/admin/**" rule would
                                                // cover it.
                                                .requestMatchers(HttpMethod.POST, "/api/auth/register/admin")
                                                .hasRole("ADMIN")

                                                // Hotel Owner-specific endpoints: Paths related to hotel owners.
                                                // Note: @PreAuthorize is used for fine-grained control like
                                                // @hotelSecurity.isHotelOwner(#hotelId)
                                                // This rule ensures only HOTEL_OWNER can access these paths generally.
                                                .requestMatchers("/api/owners/**").hasRole("HOTEL_OWNER")

                                                // --- Authenticated Endpoints (DEFAULT: Catches anything not specified
                                                // above) ---
                                                // All other requests not explicitly permitted or role-based above
                                                // require authentication.
                                                .anyRequest().authenticated() // This rule should be placed LAST for
                                                                              // general authenticated access
                                )
                                .exceptionHandling(exception -> exception
                                                // Custom handling for authentication and access denied
                                                .authenticationEntryPoint((request, response, authException) -> response
                                                                .sendError(
                                                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                                                "Unauthorized: Authentication required."))
                                                .accessDeniedHandler((request, response,
                                                                accessDeniedException) -> response.sendError(
                                                                                HttpServletResponse.SC_FORBIDDEN,
                                                                                "Forbidden: You do not have sufficient permissions to access this resource.")))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // REST API, no
                                                                                                        // sessions
                                                                                                        // needed
                                );

                // Add the JWT authentication filter before the Spring Security's default
                // UsernamePasswordAuthenticationFilter
                http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        // CORS Configuration Bean
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                // Allow specific origins for security. Add your frontend URL(s).
                // For development, localhost:4200 is common for Angular.
                configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200")); // Use Arrays.asList if
                                                                                         // multiple

                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
                configuration.setAllowCredentials(true); // Allow sending cookies/auth headers

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration); // Apply this CORS config to all paths
                return source;
        }

        // Authentication Provider Bean
        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(customUserDetailsService); // Set your custom UserDetailsService
                authProvider.setPasswordEncoder(passwordEncoder()); // Set the password encoder
                return authProvider;
        }
}
