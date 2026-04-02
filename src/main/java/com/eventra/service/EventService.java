// src/main/java/com/eventra/service/EventService.java
package com.eventra.service;

import com.eventra.domain.event.Event;
import com.eventra.domain.user.User;
import com.eventra.dto.event.CreateEventRequest;
import com.eventra.dto.event.EventResponse;
import com.eventra.dto.event.UpdateEventRequest;
import com.eventra.repository.EventRepository;
import com.eventra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final PricingService pricingService;

    @Transactional
    public EventResponse createEvent(CreateEventRequest request, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user can create event (free events remaining)
        if (!pricingService.canCreateEvent(creator.getId())) {
            throw new RuntimeException("You've used your 50 free events. Please join our waitlist for paid plans.");
        }

        Event event = Event.builder()
                .name(request.getName())
                .description(request.getDescription())
                .venue(request.getVenue())
                .venueAddress(request.getVenueAddress())
                .city(request.getCity())
                .state(request.getState())
                .eventDate(request.getEventDate())
                .dressCode(request.getDressCode())
                .expectedGuests(request.getExpectedGuests())
                .eventType(request.getEventType())
                .eventStyle(request.getEventStyle())
                .createdBy(creator.getId())
                .status("draft")
                .build();

        event = eventRepository.save(event);

        // Use one free event
        pricingService.useFreeEvent(creator.getId());

        log.info("Event created: {} by user {}", event.getId(), creator.getEmail());

        return mapToResponse(event);
    }

    public Page<EventResponse> getEvents(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return eventRepository.findByCreatedBy(user.getId(), pageable)
                .map(this::mapToResponse);
    }

    public EventResponse getEvent(UUID eventId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getCreatedBy().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return mapToResponse(event);
    }

    @Transactional
    public EventResponse updateEvent(UUID eventId, UpdateEventRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getCreatedBy().equals(user.getId())) {
            throw new RuntimeException("Only the event creator can update");
        }

        if (request.getName() != null) event.setName(request.getName());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getVenue() != null) event.setVenue(request.getVenue());
        if (request.getVenueAddress() != null) event.setVenueAddress(request.getVenueAddress());
        if (request.getCity() != null) event.setCity(request.getCity());
        if (request.getState() != null) event.setState(request.getState());
        if (request.getEventDate() != null) event.setEventDate(request.getEventDate());
        if (request.getDressCode() != null) event.setDressCode(request.getDressCode());
        if (request.getExpectedGuests() != null) event.setExpectedGuests(request.getExpectedGuests());

        event = eventRepository.save(event);

        log.info("Event updated: {} by user {}", event.getId(), user.getEmail());

        return mapToResponse(event);
    }

    @Transactional
    public void deleteEvent(UUID eventId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getCreatedBy().equals(user.getId())) {
            throw new RuntimeException("Only the event creator can delete");
        }

        eventRepository.delete(event);

        log.info("Event deleted: {} by user {}", eventId, user.getEmail());
    }

    private EventResponse mapToResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .venue(event.getVenue())
                .venueAddress(event.getVenueAddress())
                .city(event.getCity())
                .state(event.getState())
                .eventDate(event.getEventDate())
                .dressCode(event.getDressCode())
                .expectedGuests(event.getExpectedGuests())
                .eventType(event.getEventType())
                .eventStyle(event.getEventStyle())
                .status(event.getStatus())
                .createdBy(event.getCreatedBy())
                .createdAt(event.getCreatedAt())
                .build();
    }
}