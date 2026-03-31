package com.eventra.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateEventRequest {

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String venue;

    private String venueAddress;
    private String city;
    private String state;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventDate;

    private String dressCode;
    private Integer expectedGuests;
    private String eventType;
    private String eventStyle;
}