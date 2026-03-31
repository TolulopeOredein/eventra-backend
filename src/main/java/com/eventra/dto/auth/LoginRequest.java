// src/main/java/com/eventra/dto/auth/LoginRequest.java
package com.eventra.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}