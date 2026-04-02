// src/main/java/com/eventra/dto/guest/GuestResponse.java
package com.eventra.dto.guest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestResponse {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String tier;
    private String rsvpStatus;
    private LocalDateTime rsvpAt;
    private String mealPreference;
    private String dietaryRestrictions;
    private boolean asobiPaid;
    private String asobiSize;
    private boolean checkInStatus;
    private LocalDateTime checkInTime;
    private String qrCodeUrl;
}