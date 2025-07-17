// package com.hexa.cozyhavenstay.model;

package com.hexa.cozyhavenstay.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal; // Use BigDecimal for currency to avoid precision issues

@Entity
@Table(name = "user_wallets")
@Data
@NoArgsConstructor
public class UserWallet {

    @Id
    @Column(name = "USER_ID")
    private Integer userId;

    @OneToOne
    @MapsId // This maps the primary key of UserWallet to the primary key of User
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
    private User user; // Reference to the User entity

    @Column(name = "BALANCE", nullable = false, precision = 10, scale = 2)
    private BigDecimal balance; // Use BigDecimal for accuracy with money

    // Constructor to initialize with a user and default balance
    public UserWallet(User user) {
        this.user = user;
        this.userId = user.getUserId();
        this.balance = BigDecimal.ZERO; // Initialize balance to 0.00
    }
}