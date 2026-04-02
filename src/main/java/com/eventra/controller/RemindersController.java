// src/main/java/com/eventra/controller/RemindersController.java
package com.eventra.controller;

import com.eventra.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/events/{eventId}/reminders")
@RequiredArgsConstructor
public class RemindersController {

    private final ReminderService reminderService;

    /**
     * Schedule all reminders for an event (host only)
     */
    @PostMapping("/schedule")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<Void> scheduleReminders(@PathVariable UUID eventId) {
        reminderService.scheduleEventReminders(eventId);
        return ResponseEntity.ok().build();
    }

    /**
     * Manually trigger 7-day reminders (for testing)
     */
    @PostMapping("/test/7day")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> testSevenDayReminders() {
        reminderService.sendSevenDayReminders();
        return ResponseEntity.ok().build();
    }

    /**
     * Manually trigger 1-day reminders (for testing)
     */
    @PostMapping("/test/1day")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> testOneDayReminders() {
        reminderService.sendOneDayReminders();
        return ResponseEntity.ok().build();
    }

    /**
     * Manually trigger 2-hour reminders (for testing)
     */
    @PostMapping("/test/2hour")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> testTwoHourReminders() {
        reminderService.sendTwoHourReminders();
        return ResponseEntity.ok().build();
    }
}