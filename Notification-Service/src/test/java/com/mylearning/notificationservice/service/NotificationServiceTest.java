package com.mylearning.notificationservice.service;

import com.mylearning.notificationservice.dto.NotificationRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.mylearning.notificationservice.exception.NotificationProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = new NotificationRequestDto();
        requestDto.setOrderId(1L);
        requestDto.setUserId(1L);
        requestDto.setUserEmail("test@example.com");
        requestDto.setMessage("Test notification message");
    }

    @Test
    @DisplayName("Should send notification successfully with valid request")
    void sendNotification_WithValidRequest_ShouldReturnSuccessMessage() {
        // Act
        String result = notificationService.sendNotification(requestDto);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("sent to"));
        assertTrue(result.contains(requestDto.getUserEmail()));
    }

    @Test
    @DisplayName("Should throw NotificationProcessingException when request is null")
    void sendNotification_WithNullRequest_ShouldThrowException() {
        // Act & Assert
        NotificationProcessingException exception = assertThrows(NotificationProcessingException.class, 
            () -> notificationService.sendNotification(null));
        assertEquals("Notification request cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle notification with null email gracefully")
    void sendNotification_WithNullEmail_ShouldHandleGracefully() {
        // Arrange
        requestDto.setUserEmail(null);

        // Act
        String result = notificationService.sendNotification(requestDto);


        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("Notification sent to"));
    }

    @Test
    @DisplayName("Should log notification details correctly")
    void sendNotification_ShouldLogNotificationDetails() {
        // Act
        notificationService.sendNotification(requestDto);


        // Verify logging happened
        verify(logger).info(anyString(), 
            eq(requestDto.getUserEmail()), 
            eq(requestDto.getOrderId()),
            eq(requestDto.getMessage()));
    }

    @Test
    @DisplayName("Should handle notification with empty message")
    void sendNotification_WithEmptyMessage_ShouldWork() {
        // Arrange
        requestDto.setMessage("");

        // Act
        String result = notificationService.sendNotification(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals("Notification sent to " + requestDto.getUserEmail(), result);
    }
    
    @Test
    @DisplayName("Should throw NotificationProcessingException when email service fails")
    void sendNotification_WhenEmailFails_ShouldThrowException() {
        // Arrange
        requestDto.setUserEmail("fail@example.com");
        
        // Act & Assert
        NotificationProcessingException exception = assertThrows(NotificationProcessingException.class,
            () -> notificationService.sendNotification(requestDto));
            
        assertTrue(exception.getMessage().contains("Failed to send notification to fail@example.com"));
    }
}
