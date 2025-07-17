// src/main/java/com/hexa/cozyhavenstay/model/User.java
package com.hexa.cozyhavenstay.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode; // Ensure this is imported
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore; // Ensure this is imported for password

import java.math.BigDecimal; // Import BigDecimal
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "USERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "ownedHotels", "bookings", "reviews" })
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Integer userId;

    @Column(name = "USERNAME", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "PASSWORD_HASH", nullable = false, length = 512)
    @JsonIgnore // Good for security, as you had it
    private String password; // Mapped to PASSWORD_HASH

    @Column(name = "FIRST_NAME", nullable = false, length = 100)
    private String firstName;

    @Column(name = "LAST_NAME", nullable = false, length = 100)
    private String lastName;

    @Column(name = "FULL_NAME", insertable = false, updatable = false)
    private String fullName; // As per your DB, it's a virtual column

    @Column(name = "GENDER", length = 10)
    private String gender;

    @Column(name = "COUNTRY_CODE", nullable = false, length = 5)
    private String countryCode;

    @Column(name = "LOCAL_PHONE_NUMBER", nullable = false, unique = true, length = 20)
    private String localPhoneNumber;

    @Column(name = "ADDRESS", length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "USER_TYPE", nullable = false, length = 20)
    private RoleType role; // Using your RoleType enum

    @Column(name = "REGISTRATION_DATE", nullable = false, updatable = false)
    private LocalDateTime registrationDate;

    @Column(name = "LAST_LOGIN_DATE")
    private LocalDateTime lastLoginDate;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive;

    // ⭐ CORRECT PLACEMENT AND ANNOTATION FOR BALANCE ⭐
    @Column(name = "BALANCE", nullable = false, precision = 10, scale = 2) // Ensure precision and scale match DB
    private BigDecimal balance; // Initialized in @PrePersist for new users, or via setter

    @OneToMany(mappedBy = "ownerUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("user-ownedHotels")
    private Set<Hotel> ownedHotels;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("user-bookings")
    private Set<Booking> bookings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("user-reviews")
    private Set<Review> reviews;

    @PrePersist
    protected void onCreate() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        // ⭐ Initialize balance for new users ⭐
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // No specific updates needed for balance in @PreUpdate here, handled by service logic
    }

    // UserDetails interface methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username; // This is typically for Spring Security authentication
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    public Integer getUserId() {
        return userId;
    }
}