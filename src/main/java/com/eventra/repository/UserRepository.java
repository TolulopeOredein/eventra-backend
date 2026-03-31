// src/main/java/com/eventra/repository/UserRepository.java
package com.eventra.repository;

import com.eventra.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    // Remove this line: Optional<User> findByPhoneNormalized(String phoneNormalized);

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    Page<User> findByRole(String role, Pageable pageable);
    Page<User> findByStatus(String status, Pageable pageable);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.email = :email")
    void incrementFailedAttempts(String email);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lockedUntil = null WHERE u.email = :email")
    void resetFailedAttempts(String email);

    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = :lockedUntil WHERE u.email = :email")
    void lockUser(String email, LocalDateTime lockedUntil);
}