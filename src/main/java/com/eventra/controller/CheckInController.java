// src/main/java/com/eventra/controller/CheckInController.java
package com.eventra.controller;

import com.eventra.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyQr(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {

        String qrData = request.get("qrData");
        UUID eventId = UUID.fromString(request.get("eventId"));
        String gate = request.get("gate");

        Map<String, Object> result = checkInService.verifyAndCheckIn(qrData, eventId, deviceId, gate);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncOfflineCheckins(
            @RequestBody Map<String, Object> request,
            @RequestHeader("X-Device-Id") String deviceId) {

        List<Map<String, Object>> checkins = (List<Map<String, Object>>) request.get("checkins");
        checkInService.syncOfflineCheckins(checkins, deviceId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats/{eventId}")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable UUID eventId) {
        long checkedIn = checkInService.getCheckedInCount(eventId);
        double gateRate = checkInService.getCurrentGateRate(eventId, 30);

        return ResponseEntity.ok(Map.of(
                "checkedIn", checkedIn,
                "gateRate", gateRate
        ));
    }
}