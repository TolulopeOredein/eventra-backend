//// src/main/java/com/eventra/service/ReminderConfigService.java
//package com.eventra.service;
//
//import com.eventra.domain.event.Event;
//import com.eventra.domain.guest.Guest;
//import com.eventra.repository.EventRepository;
//import com.eventra.repository.GuestRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class ReminderConfigService {
//
//    private final EventRepository eventRepository;
//    private final GuestRepository guestRepository;
//    private final NotificationService notificationService;
//    private final TrafficService trafficService;
//
//    // Store reminder settings per event
//    private final Map<String, ReminderSettings> reminderSettings = new HashMap<>();
//
//    /**
//     * Configure reminders for an event
//     */
//    public void configureReminders(UUID eventId, ReminderSettings settings) {
//        reminderSettings.put(eventId.toString(), settings);
//        log.info("Reminders configured for event: {}", eventId);
//    }
//
//    @Scheduled(cron = "0 0 9 * * *") // Daily at 9 AM
//    public void sendReminders() {
//        LocalDateTime today = LocalDateTime.now();
//        List<Event> events = eventRepository.findByEventDateBetween(
//                today, today.plusDays(7));
//
//        for (Event event : events) {
//            ReminderSettings settings = reminderSettings.getOrDefault(
//                    event.getId().toString(),
//                    getDefaultSettings()
//            );
//
//            long daysUntilEvent = java.time.temporal.ChronoUnit.DAYS.between(
//                    today, event.getEventDate());
//
//            if (settings.isRsvpReminder() && daysUntilEvent == 7) {
//                sendRsvpReminders(event);
//            }
//
//            if (settings.isAsobiReminder() && daysUntilEvent == 3) {
//                sendAsobiReminders(event);
//            }
//
//            if (settings.isEventReminder() && daysUntilEvent == 1) {
//                sendEventReminders(event);
//            }
//        }
//    }
//
//    @Scheduled(cron = "0 0 12 * * *") // Daily at 12 PM
//    public void sendTrafficReminders() {
//        LocalDateTime now = LocalDateTime.now();
//        List<Event> events = eventRepository.findByEventDateBetween(
//                now.plusHours(2), now.plusHours(3));
//
//        for (Event event : events) {
//            ReminderSettings settings = reminderSettings.getOrDefault(
//                    event.getId().toString(),
//                    getDefaultSettings()
//            );
//
//            if (settings.isTrafficReminder()) {
//                sendTrafficReminders(event);
//            }
//        }
//    }
//
//    private void sendRsvpReminders(Event event) {
//        List<Guest> pendingGuests = guestRepository.findByEventIdAndRsvpStatus(
//                event.getId(), "pending");
//
//        for (Guest guest : pendingGuests) {
//            notificationService.sendReminder(guest, event, "RSVP");
//        }
//        log.info("Sent RSVP reminders to {} guests for event: {}",
//                pendingGuests.size(), event.getId());
//    }
//
//    private void sendAsobiReminders(Event event) {
//        List<Guest> guests = guestRepository.findByEventId(event.getId());
//        int count = 0;
//
//        for (Guest guest : guests) {
//            if (!guest.isAsobiPaid() && guest.getAsobiSize() != null) {
//                notificationService.sendReminder(guest, event, "ASOBI");
//                count++;
//            }
//        }
//        log.info("Sent Aso-ebi reminders to {} guests for event: {}", count, event.getId());
//    }
//
//    private void sendEventReminders(Event event) {
//        List<Guest> guests = guestRepository.findByEventIdAndRsvpStatus(
//                event.getId(), "confirmed");
//
//        for (Guest guest : guests) {
//            notificationService.sendReminder(guest, event, "EVENT");
//        }
//        log.info("Sent event reminders to {} guests for event: {}", guests.size(), event.getId());
//    }
//
//    private void sendTrafficReminders(Event event) {
//        List<Guest> guests = guestRepository.findByEventIdAndCheckInStatusFalse(
//                event.getId());
//
//        for (Guest guest : guests) {
//            var advisory = trafficService.getTrafficAdvisory(guest, event);
//            String message = buildTrafficMessage(guest, event, advisory);
//            notificationService.sendWhatsApp(guest.getPhone(), message);
//        }
//        log.info("Sent traffic reminders to {} guests for event: {}", guests.size(), event.getId());
//    }
//
//    private String buildTrafficMessage(Guest guest, Event event, TrafficService.TrafficAdvisory advisory) {
//        return String.format("""
//            🚗 *Traffic Alert for %s*
//
//            Dear %s,
//
//            Based on current traffic conditions, we recommend leaving by %s to arrive at %s.
//
//            Estimated travel time: %s
//            Traffic level: %s
//
//            View route: https://maps.google.com/?q=%s
//
//            Safe travels! 🚗
//            """,
//                event.getName(),
//                guest.getName(),
//                advisory.getRecommendedDepartureText(),
//                advisory.getArrivalTimeText(),
//                advisory.getDurationInTrafficText(),
//                advisory.getTrafficLevel(),
//                event.getVenueAddress()
//        );
//    }
//
//    private ReminderSettings getDefaultSettings() {
//        return ReminderSettings.builder()
//                .rsvpReminder(true)
//                .asobiReminder(true)
//                .eventReminder(true)
//                .trafficReminder(true)
//                .build();
//    }
//
//    @lombok.Data
//    @lombok.Builder
//    public static class ReminderSettings {
//        private boolean rsvpReminder;
//        private boolean asobiReminder;
//        private boolean eventReminder;
//        private boolean trafficReminder;
//    }
//}