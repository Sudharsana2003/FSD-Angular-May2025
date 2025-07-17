// package com.hexa.cozyhavenstay.repository;

package com.hexa.cozyhavenstay.repository;

import com.hexa.cozyhavenstay.model.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface UserWalletRepository extends JpaRepository<UserWallet, Integer> {

    // Custom query to find and lock the wallet row for a given user ID
    // PESSIMISTIC_WRITE lock ensures exclusive access during a transaction
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT uw FROM UserWallet uw WHERE uw.userId = :userId")
    Optional<UserWallet> findByUserIdWithLock(@Param("userId") Integer userId);

    Optional<UserWallet> findByUserId(Integer userId);
}