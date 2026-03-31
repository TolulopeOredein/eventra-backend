// src/main/java/com/eventra/dto/auth/AuthResponse.java
package com.eventra.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfo user;

    @Data
    @Builder
    public static class UserInfo {
        private String id;
        private String email;
        private String phone;
        private String firstName;
        private String lastName;
        private String role;
        private int verificationLevel;
    }
}