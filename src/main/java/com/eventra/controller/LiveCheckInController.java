// src/main/java/com/eventra/controller/LiveCheckInController.java
package com.eventra.controller;

import com.eventra.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class LiveCheckInController {

    private final CheckInService checkInService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/checkin/{eventId}")
    @SendTo("/topic/checkin/{eventId}")
    public CheckInEvent checkIn(Map<String, String> message) {
        UUID eventId = UUID.fromString(message.get("eventId"));
        String qrData = message.get("qrData");
        String gate = message.get("gate");
        String deviceId = message.get("deviceId");

        Map<String, Object> result = checkInService.verifyAndCheckIn(qrData, eventId, deviceId, gate);

        // Broadcast to all connected clients
        messagingTemplate.convertAndSend("/topic/checkin/" + eventId + "/stats",
                checkInService.getLiveStats(eventId));

        return CheckInEvent.builder()
                .guestName((String) result.get("guestName"))
                .tier((String) result.get("tier"))
                .gate(gate)
                .timestamp(result.get("checkInTime").toString())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class CheckInEvent {
        private String guestName;
        private String tier;
        private String gate;
        private String timestamp;
    }
}