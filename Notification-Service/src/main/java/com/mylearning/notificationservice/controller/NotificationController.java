package com.mylearning.notificationservice.controller;

import com.mylearning.notificationservice.dto.NotificationRequestDto;
import com.mylearning.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Notification Service is running");
    }

    @PostMapping
    public ResponseEntity<String> notifyOrderSuccess(@Valid @RequestBody NotificationRequestDto request) {
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }
}