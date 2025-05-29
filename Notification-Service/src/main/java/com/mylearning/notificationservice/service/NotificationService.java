package com.mylearning.notificationservice.service;

import com.mylearning.notificationservice.dto.NotificationRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {
    public String sendNotification(NotificationRequestDto request) {
        log.info("Sending notification to {} for Order ID {}: {}", request.getUserEmail(), request.getOrderId(), request.getMessage());
        return "Notification sent to " + request.getUserEmail();
    }
}
