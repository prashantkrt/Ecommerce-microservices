package com.mylearning.notificationservice.service;

import com.mylearning.notificationservice.dto.NotificationRequestDto;
import com.mylearning.notificationservice.exception.NotificationProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;
    
    private NotificationRequestDto requestDto;

    @BeforeEach
    void setUp() {
        // Set up test data
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
        assertTrue(exception.getMessage().contains("Notification request cannot be null"));
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
        assertEquals("Notification sent to user", result);
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
    
    @Test
    @DisplayName("Should handle notification with very long message")
    void sendNotification_WithLongMessage_ShouldWork() {
        // Arrange
        String longMessage = "A".repeat(1000);
        requestDto.setMessage(longMessage);
        
        // Act
        String result = notificationService.sendNotification(requestDto);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("sent to"));
        assertTrue(result.contains(requestDto.getUserEmail()));
    }
    
    @Test
    @DisplayName("Should handle notification with special characters in email")
    void sendNotification_WithSpecialCharacters_ShouldWork() {
        // Arrange
        requestDto.setUserEmail("user+test@example.com");
        
        // Act
        String result = notificationService.sendNotification(requestDto);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("user+test@example.com"));
    }
    
    @Test
    @DisplayName("Should handle notification with null order ID")
    void sendNotification_WithNullOrderId_ShouldWork() {
        // Arrange
        requestDto.setOrderId(null);
        
        // Act
        String result = notificationService.sendNotification(requestDto);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("sent to"));
    }
    
    @Test
    @DisplayName("Should handle notification with null user ID")
    void sendNotification_WithNullUserId_ShouldWork() {
        // Arrange
        requestDto.setUserId(null);
        
        // Act
        String result = notificationService.sendNotification(requestDto);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("sent to"));
    }
}
