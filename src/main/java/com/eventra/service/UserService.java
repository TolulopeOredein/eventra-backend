// src/main/java/com/eventra/service/UserService.java
package com.eventra.service;

import com.eventra.domain.user.User;
import com.eventra.dto.auth.RegisterRequest;
import com.eventra.dto.auth.LoginRequest;
import com.eventra.dto.auth.AuthResponse;
import com.eventra.repository.UserRepository;
import com.eventra.security.JwtService;
import com.eventra.security.CustomUserDetailsService;
import com.eventra.util.PhoneNumberUtil;
import com.eventra.integration.twilio.TwilioClient;
import com.eventra.integration.resend.ResendClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final PhoneNumberUtil phoneNumberUtil;
    private final TwilioClient twilioClient;
    private final ResendClient resendClient;

    /**
     * Register a new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Validate phone uniqueness
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone already registered");
        }

        // Normalize phone number to E.164 format
        String normalizedPhone = phoneNumberUtil.normalize(request.getPhone());

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .phoneNormalized(normalizedPhone)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role("host")
                .status("active")
                .verificationLevel(0)
                .subscriptionTier("free")
                .subscriptionStatus("active")
                .freeEventsRemaining(50)
                .totalEventsCreated(0)
                .build();

        user = userRepository.save(user);

        log.info("New user registered: {} ({})", user.getEmail(), user.getId());

        // Send welcome message via WhatsApp
        try {
            String welcomeMessage = String.format(
                    "🎉 Welcome to Eventra, %s! 🎉\n\n" +
                            "Your account has been created successfully.\n\n" +
                            "✨ You have 50 FREE events to get started!\n" +
                            "✨ Create your first event now: https://eventra.ng/events/new\n\n" +
                            "Need help? Reply HELP or visit our help center.\n\n" +
                            "Let's create amazing events together! 🎊",
                    user.getFirstName()
            );
            twilioClient.sendWhatsApp(user.getPhone(), welcomeMessage);
        } catch (Exception e) {
            log.warn("Failed to send WhatsApp welcome message to {}: {}", user.getPhone(), e.getMessage());
        }

        // Send welcome email
        try {
            String emailSubject = "Welcome to Eventra! 🎉";
            String emailBody = String.format(
                    "Welcome to Eventra, %s!\n\n" +
                            "Your account has been created successfully.\n\n" +
                            "You have 50 FREE events to get started!\n" +
                            "Create your first event here: https://eventra.ng/events/new\n\n" +
                            "Need help? Check our documentation or reply to this email.\n\n" +
                            "Let's create amazing events together!\n\n" +
                            "Best regards,\n" +
                            "The Eventra Team",
                    user.getFirstName()
            );
            resendClient.sendEmail(user.getEmail(), emailSubject, emailBody, null);
        } catch (Exception e) {
            log.warn("Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage());
        }

        // Generate tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    /**
     * Login user
     */
    // src/main/java/com/eventra/service/UserService.java
// Update the login method to accept LoginRequest

    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Find user by email or phone
        User user = userRepository.findByEmail(request.getUsername())
                .or(() -> userRepository.findByPhone(request.getUsername()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if account is locked
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Account locked. Please try again later.");
        }

        // Reset failed login attempts on successful login
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in: {} ({})", user.getEmail(), user.getId());

        // Generate tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // Add this overloaded method to UserService.java
    public AuthResponse login(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return login(request);
    }

    /**
     * Refresh access token
     */
    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("Token refreshed for user: {}", user.getEmail());

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    /**
     * Logout user (invalidate token)
     */
    public void logout(String token) {
        // In production, add token to blacklist in Redis
        log.info("User logged out, token invalidated");
    }

    /**
     * Get user by ID
     */
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Update user profile
     */
    @Transactional
    public User updateUserProfile(UUID userId, String firstName, String lastName, String phone) {
        User user = getUserById(userId);

        if (firstName != null && !firstName.isEmpty()) {
            user.setFirstName(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            user.setLastName(lastName);
        }
        if (phone != null && !phone.isEmpty()) {
            String normalizedPhone = phoneNumberUtil.normalize(phone);
            user.setPhone(phone);
            user.setPhoneNormalized(normalizedPhone);
        }

        user = userRepository.save(user);
        log.info("User profile updated: {}", user.getEmail());

        return user;
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getEmail());
    }

    /**
     * Record failed login attempt
     */
    @Transactional
    public void recordFailedLoginAttempt(String username) {
        User user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByPhone(username))
                .orElse(null);

        if (user != null) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            // Lock account after 5 failed attempts
            if (attempts >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
                log.warn("Account locked for user: {} due to 5 failed login attempts", user.getEmail());
            }

            userRepository.save(user);
        }
    }

    /**
     * Get remaining free events for user
     */
    public int getRemainingFreeEvents(UUID userId) {
        User user = getUserById(userId);
        return user.getFreeEventsRemaining();
    }

    /**
     * Get user's subscription tier
     */
    public String getSubscriptionTier(UUID userId) {
        User user = getUserById(userId);
        return user.getSubscriptionTier();
    }

    /**
     * Build authentication response
     */
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L) // 15 minutes
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role(user.getRole())
                        .verificationLevel(user.getVerificationLevel())
                        .subscriptionTier(user.getSubscriptionTier())
                        .freeEventsRemaining(user.getFreeEventsRemaining())
                        .build())
                .build();
    }
}