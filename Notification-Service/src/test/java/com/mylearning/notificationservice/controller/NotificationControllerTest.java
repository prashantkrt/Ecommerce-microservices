package com.mylearning.notificationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylearning.notificationservice.dto.NotificationRequestDto;
import com.mylearning.notificationservice.exception.NotificationProcessingException;
import com.mylearning.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
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
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();

        requestDto = new NotificationRequestDto();
        requestDto.setOrderId(1L);
        requestDto.setUserId(1L);
        requestDto.setUserEmail("test@example.com");
        requestDto.setMessage("Test notification message");
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
                .andExpect(content().string(successMessage));
    }

    @Test
    @DisplayName("POST /api/notifications - Should throw exception for invalid request")
    void notifyOrderSuccess_WithInvalidRequest_ShouldThrowException() {
        // Arrange
        requestDto.setMessage(null); // Invalid: message is required

        // Act & Assert
        Exception exception = assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(post("/api/notifications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)));
        });
        
        assertTrue(exception.getCause() instanceof jakarta.validation.ConstraintViolationException);
    }

    @Test
    @DisplayName("POST /api/notifications - Should throw exception for missing required fields")
    void notifyOrderSuccess_WithMissingRequiredFields_ShouldThrowException() {
        // Arrange
        requestDto.setOrderId(null); // Missing required field
        requestDto.setUserId(null);   // Missing required field

        // Act & Assert
        Exception exception = assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(post("/api/notifications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)));
        });
        
        assertTrue(exception.getCause() instanceof jakarta.validation.ConstraintViolationException);
    }

    @Test
    @DisplayName("POST /api/notifications - Should propagate service exceptions")
    void notifyOrderSuccess_WhenServiceThrowsException_ShouldPropagate() {
        // Arrange
        String errorMessage = "Failed to send notification to test@example.com";
        when(notificationService.sendNotification(any(NotificationRequestDto.class)))
                .thenThrow(new NotificationProcessingException(errorMessage));

        // Act & Assert
        Exception exception = assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(post("/api/notifications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)));
        });
        
        assertTrue(exception.getCause() instanceof NotificationProcessingException);
        assertEquals(errorMessage, exception.getCause().getMessage());
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
                .andExpect(content().string(successMessage));
    }
}
