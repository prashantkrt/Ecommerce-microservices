package com.mylearning.notificationservice.service;

import com.mylearning.notificationservice.dto.NotificationRequestDto;
import com.mylearning.notificationservice.exception.NotificationProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {
    public String sendNotification(NotificationRequestDto request) {
        try {
            if (request == null) {
                throw new NotificationProcessingException("Notification request cannot be null");
            }
            
            log.info("Sending notification to {} for Order ID {}: {}", 
                request.getUserEmail(), 
                request.getOrderId(), 
                request.getMessage());
                
            // Simulate potential notification failure (e.g., email service down)
            if (request.getUserEmail() != null && request.getUserEmail().contains("fail")) {
                throw new NotificationProcessingException("Failed to send notification to " + request.getUserEmail());
            }
            
            return "Notification sent to " + (request.getUserEmail() != null ? request.getUserEmail() : "user");
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage());
            throw new NotificationProcessingException("Failed to process notification: " + e.getMessage(), e);
        }
    }
}
