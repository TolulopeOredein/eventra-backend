package com.eventra.dto.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EventResponse {
    private UUID id;
    private String name;
    private String description;
    private String venue;
    private String venueAddress;
    private String city;
    private String state;
    private LocalDateTime eventDate;
    private String dressCode;
    private Integer expectedGuests;
    private String eventType;
    private String eventStyle;
    private String status;
    private UUID createdBy;
    private LocalDateTime createdAt;
}