// src/main/java/com/eventra/service/ReminderService.java
package com.eventra.service;

import com.eventra.domain.event.Event;
import com.eventra.repository.EventRepository;
import com.eventra.domain.guest.Guest;
import com.eventra.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final EventRepository eventRepository;
    private final GuestRepository guestRepository;
    private final NotificationService notificationService;
    private final TrafficService trafficService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final ConcurrentHashMap<UUID, Boolean> scheduledReminders = new ConcurrentHashMap<>();

    /**
     * Schedule all reminders for an event
     */
    public void scheduleEventReminders(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Schedule 7-day reminder
        scheduleReminder(event, 7, TimeUnit.DAYS, "RSVP");

        // Schedule 3-day reminder (Aso-ebi)
        scheduleReminder(event, 3, TimeUnit.DAYS, "ASOBI");

        // Schedule 1-day reminder
        scheduleReminder(event, 1, TimeUnit.DAYS, "EVENT");

        // Schedule 2-hour reminder
        scheduleReminder(event, 2, TimeUnit.HOURS, "TRAFFIC");

        // Schedule 30-minute reminder
        scheduleReminder(event, 30, TimeUnit.MINUTES, "URGENT");

        log.info("Scheduled reminders for event: {}", eventId);
    }

    /**
     * Schedule a single reminder
     */
    // src/main/java/com/eventra/service/ReminderService.java
// Fix the scheduleReminder method (line 63)

    private void scheduleReminder(Event event, long delay, TimeUnit unit, String reminderType) {
        LocalDateTime reminderTime;

        // Calculate reminder time based on unit
        switch (unit) {
            case DAYS:
                reminderTime = event.getEventDate().minusDays(delay);
                break;
            case HOURS:
                reminderTime = event.getEventDate().minusHours(delay);
                break;
            case MINUTES:
                reminderTime = event.getEventDate().minusMinutes(delay);
                break;
            default:
                reminderTime = event.getEventDate().minusDays(delay);
        }

        if (reminderTime.isAfter(LocalDateTime.now())) {
            long delayMillis = reminderTime.toEpochSecond(java.time.ZoneOffset.UTC) * 1000 -
                    System.currentTimeMillis();

            scheduler.schedule(() -> {
                sendReminderForEvent(event, reminderType);
            }, delayMillis, TimeUnit.MILLISECONDS);

            log.info("Scheduled {} reminder for event {} at {}", reminderType, event.getId(), reminderTime);
        }
    }

    /**
     * Send reminders to all confirmed guests for an event
     */
    private void sendReminderForEvent(Event event, String reminderType) {
        List<Guest> guests = guestRepository.findByEventIdAndRsvpStatus(event.getId(), "confirmed");

        for (Guest guest : guests) {
            try {
                if ("TRAFFIC".equals(reminderType)) {
                    var advisory = trafficService.getTrafficAdvisory(guest, event);
                    // Send traffic-specific reminder
                    String trafficMessage = buildTrafficMessage(guest, event, advisory);
                    notificationService.sendReminder(guest, event, "TRAFFIC");
                } else {
                    notificationService.sendReminder(guest, event, reminderType);
                }
            } catch (Exception e) {
                log.error("Failed to send {} reminder to {}: {}", reminderType, guest.getPhone(), e.getMessage());
            }
        }

        log.info("Sent {} reminders for event {} to {} guests", reminderType, event.getId(), guests.size());
    }

    /**
     * Build traffic-specific reminder message
     */
    private String buildTrafficMessage(Guest guest, Event event, TrafficService.TrafficAdvisory advisory) {
        return String.format("""
            🚗 TRAFFIC ADVISORY for %s
            Dear %s,
            
            To arrive by %s, we recommend leaving by %s.
            
            Current conditions: %s traffic
            Expected travel time: %s
            
            View route: https://maps.google.com/?q=%s
            
            Safe travels! 🚗
            """,
                event.getName(),
                guest.getName(),
                event.getEventDate().format(java.time.format.DateTimeFormatter.ofPattern("h:mm a")),
                advisory.getRecommendedDepartureText(),
                advisory.getTrafficLevel(),
                advisory.getDurationInTrafficText(),
                advisory.getDestination()
        );
    }

    // ==================== SCHEDULED JOBS ====================

    /**
     * Run daily at 9 AM - send 7-day reminders
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendSevenDayReminders() {
        LocalDateTime sevenDaysFromNow = LocalDateTime.now().plusDays(7);
        List<Event> events = eventRepository.findByEventDateBetween(
                sevenDaysFromNow.minusHours(12),
                sevenDaysFromNow.plusHours(12));

        for (Event event : events) {
            List<Guest> pendingGuests = guestRepository.findByEventIdAndRsvpStatus(event.getId(), "pending");
            for (Guest guest : pendingGuests) {
                notificationService.sendReminder(guest, event, "RSVP");
            }
            log.info("Sent 7-day reminders for event: {}", event.getId());
        }
    }

    /**
     * Run daily at 10 AM - send 3-day reminders (Aso-ebi)
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void sendThreeDayReminders() {
        LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);
        List<Event> events = eventRepository.findByEventDateBetween(
                threeDaysFromNow.minusHours(12),
                threeDaysFromNow.plusHours(12));

        for (Event event : events) {
            List<Guest> guests = guestRepository.findByEventId(event.getId());
            for (Guest guest : guests) {
                if (guest.getAsobiSize() != null && !guest.isAsobiPaid()) {
                    notificationService.sendReminder(guest, event, "ASOBI");
                }
            }
            log.info("Sent 3-day reminders for event: {}", event.getId());
        }
    }

    /**
     * Run daily at 10 AM - send 1-day reminders
     */
    @Scheduled(cron = "0 0 11 * * *")
    public void sendOneDayReminders() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        List<Event> events = eventRepository.findByEventDateBetween(
                tomorrow.minusHours(12),
                tomorrow.plusHours(12));

        for (Event event : events) {
            List<Guest> guests = guestRepository.findByEventIdAndRsvpStatus(event.getId(), "confirmed");
            for (Guest guest : guests) {
                notificationService.sendReminder(guest, event, "EVENT");
            }
            log.info("Sent 1-day reminders for event: {}", event.getId());
        }
    }

    /**
     * Run daily at 12 PM - send 2-hour reminders with traffic
     */
    @Scheduled(cron = "0 0 12 * * *")
    public void sendTwoHourReminders() {
        LocalDateTime twoHoursFromNow = LocalDateTime.now().plusHours(2);
        List<Event> events = eventRepository.findByEventDateBetween(
                twoHoursFromNow.minusMinutes(30),
                twoHoursFromNow.plusMinutes(30));

        for (Event event : events) {
            List<Guest> guests = guestRepository.findByEventIdAndCheckInStatusFalse(event.getId());
            for (Guest guest : guests) {
                notificationService.sendReminder(guest, event, "TRAFFIC");
            }
            log.info("Sent 2-hour reminders for event: {}", event.getId());
        }
    }

    /**
     * Run every 30 minutes - send urgent reminders for events starting soon
     */
    @Scheduled(cron = "0 */30 * * * *")
    public void sendThirtyMinuteReminders() {
        LocalDateTime thirtyMinutesFromNow = LocalDateTime.now().plusMinutes(30);
        List<Event> events = eventRepository.findByEventDateBetween(
                thirtyMinutesFromNow.minusMinutes(15),
                thirtyMinutesFromNow.plusMinutes(15));

        for (Event event : events) {
            List<Guest> guests = guestRepository.findByEventIdAndCheckInStatusFalse(event.getId());
            for (Guest guest : guests) {
                notificationService.sendReminder(guest, event, "URGENT");
            }
            log.info("Sent urgent reminders for event: {}", event.getId());
        }
    }
}