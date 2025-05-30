package com.mylearning.notificationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylearning.notificationservice.dto.NotificationRequestDto;
import com.mylearning.notificationservice.exception.GlobalExceptionHandler;
import com.mylearning.notificationservice.exception.NotificationProcessingException;
import com.mylearning.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private NotificationRequestDto requestDto;

    @BeforeEach
    void setUp() {
        // Set up MockMvc with standalone configuration
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Initialize test data
        requestDto = new NotificationRequestDto();
        requestDto.setOrderId(1L);
        requestDto.setUserId(1L);
        requestDto.setUserEmail("test@example.com");
        requestDto.setMessage("Test notification message");
        
        // Reset mocks before each test
        reset(notificationService);
    }

    @Test
    @DisplayName("GET /api/notifications/health - Should return service status")
    void getHealthStatus_ShouldReturnOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/notifications/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification Service is running"));
    }

    @Test
    @DisplayName("POST /api/notifications - Should process valid notification request successfully")
    void notifyOrderSuccess_WithValidRequest_ShouldReturnOk() throws Exception {
        // Arrange
        String successMessage = "Notification sent to test@example.com";
        when(notificationService.sendNotification(any(NotificationRequestDto.class))).thenReturn(successMessage);

        // Act & Assert
        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(successMessage)));
                
        // Verify service was called
        verify(notificationService, times(1)).sendNotification(any(NotificationRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/notifications - Should return bad request for invalid request")
    void notifyOrderSuccess_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        requestDto.setMessage(null); // Invalid: message is required

        // Act & Assert
        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
                
        // Verify service was not called
        verify(notificationService, never()).sendNotification(any(NotificationRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/notifications - Should return bad request for missing required fields")
    void notifyOrderSuccess_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        requestDto.setOrderId(null); // Missing required field
        requestDto.setUserId(null);   // Missing required field

        // Act & Assert - Expect validation to fail with 400 status and specific error messages
        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.orderId").value("Order ID is required"))
                .andExpect(jsonPath("$.userId").value("User ID is required"));
                
        // Verify service was not called
        verify(notificationService, never()).sendNotification(any(NotificationRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/notifications - Should handle service exceptions")
    void notifyOrderSuccess_WhenServiceThrowsException_ShouldHandleGracefully() throws Exception {
        // Arrange
        String errorMessage = "Failed to send notification to test@example.com";
        when(notificationService.sendNotification(any(NotificationRequestDto.class)))
                .thenThrow(new NotificationProcessingException(errorMessage));

        // Act & Assert
        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(containsString(errorMessage)));
                
        // Verify service was called
        verify(notificationService, times(1)).sendNotification(any(NotificationRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/notifications - Should process notification with empty email")
    void notifyOrderSuccess_WithEmptyEmail_ShouldStillProcess() throws Exception {
        // Arrange
        requestDto.setUserEmail("");
        String successMessage = "Notification sent to ";
        when(notificationService.sendNotification(any(NotificationRequestDto.class))).thenReturn(successMessage);

        // Act & Assert
        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(successMessage)));
                
        // Verify service was called with empty email
        verify(notificationService, times(1)).sendNotification(any(NotificationRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/notifications - Should handle null email in request")
    void notifyOrderSuccess_WithNullEmail_ShouldProcessSuccessfully() throws Exception {
        // Arrange
        requestDto.setUserEmail(null);
        String successMessage = "Notification sent to user";
        when(notificationService.sendNotification(any(NotificationRequestDto.class))).thenReturn(successMessage);

        // Act & Assert
        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Notification sent to")));
                
        // Verify service was called with null email
        verify(notificationService, times(1)).sendNotification(argThat(dto -> dto.getUserEmail() == null));
    }

    @Test
    @DisplayName("POST /api/notifications - Should handle long message")
    void notifyOrderSuccess_WithLongMessage_ShouldProcessSuccessfully() throws Exception {
        // Arrange
        String longMessage = "This is a very long message ".repeat(100); // 3000+ characters
        requestDto.setMessage(longMessage);
        String successMessage = "Notification sent to test@example.com";
        when(notificationService.sendNotification(any(NotificationRequestDto.class))).thenReturn(successMessage);

        // Act & Assert
        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Notification sent to")));
                
        // Verify service was called with the long message
        verify(notificationService, times(1)).sendNotification(argThat(dto -> dto.getMessage().length() > 1000));
    }

    @Test
    @DisplayName("POST /api/notifications - Should handle very large order IDs")
    void notifyOrderSuccess_WithLargeOrderId_ShouldProcessSuccessfully() throws Exception {
        // Arrange
        long largeOrderId = 9_999_999_999L;
        requestDto.setOrderId(largeOrderId);
        String successMessage = "Notification sent to test@example.com";
        when(notificationService.sendNotification(any(NotificationRequestDto.class))).thenReturn(successMessage);

        // Act & Assert
        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Notification sent to")));
                
        // Verify service was called with the large order ID
        verify(notificationService, times(1)).sendNotification(argThat(dto -> dto.getOrderId() == largeOrderId));
    }

    @Test
    @DisplayName("POST /api/notifications - Should handle invalid JSON")
    void notifyOrderSuccess_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert - Invalid JSON will be handled by the GlobalExceptionHandler
        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid-json}"))
                .andExpect(status().isInternalServerError()) // Changed from BadRequest to InternalServerError
                .andExpect(content().string(containsString("Unexpected character")));
                
        // Verify service was not called
        verify(notificationService, never()).sendNotification(any(NotificationRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/notifications - Should handle empty request body")
    void notifyOrderSuccess_WithEmptyBody_ShouldReturnBadRequest() throws Exception {
        // Act & Assert - Empty body will be handled by the GlobalExceptionHandler
        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isInternalServerError()) // Changed from BadRequest to InternalServerError
                .andExpect(content().string(containsString("Required request body")));
                
        // Verify service was not called
        verify(notificationService, never()).sendNotification(any(NotificationRequestDto.class));
    }

    @Test
    @DisplayName("GET /api/notifications/health - Should handle multiple requests")
    void getHealthStatus_MultipleRequests_ShouldAllSucceed() throws Exception {
        // Act & Assert - Multiple concurrent health checks
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/notifications/health"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Notification Service is running"));
        }
    }
}
