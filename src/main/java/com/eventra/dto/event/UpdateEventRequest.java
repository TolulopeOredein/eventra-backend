package com.eventra.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateEventRequest {
    private String name;
    private String description;
    private String venue;
    private String venueAddress;
    private String city;
    private String state;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventDate;
    private String dressCode;
    private Integer expectedGuests;
}