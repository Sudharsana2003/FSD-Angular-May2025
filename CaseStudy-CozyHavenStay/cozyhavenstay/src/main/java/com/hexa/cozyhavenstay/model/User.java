package com.hexa.cozyhavenstay.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference; // Ensure this is imported
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "USERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"ownedHotels", "bookings", "reviews"})
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
    @JsonIgnore
    private String password;

    @Column(name = "FIRST_NAME", nullable = false, length = 100)
    private String firstName;

    @Column(name = "LAST_NAME", nullable = false, length = 100)
    private String lastName;

    @Column(name = "FULL_NAME", insertable = false, updatable = false)
    private String fullName;

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
    private RoleType role;

    @Column(name = "REGISTRATION_DATE", nullable = false, updatable = false)
    private LocalDateTime registrationDate;

    @Column(name = "LAST_LOGIN_DATE")
    private LocalDateTime lastLoginDate;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive;

    @OneToMany(mappedBy = "ownerUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("user-ownedHotels") // <--- CHANGED: This is now the "managing" side
    private Set<Hotel> ownedHotels;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("user-bookings")
    private Set<Booking> bookings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("user-reviews")
    private Set<Review> reviews;


    @PrePersist
    @PreUpdate
    protected void updateTimestampsAndStatus() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
    }

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
        return username;
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