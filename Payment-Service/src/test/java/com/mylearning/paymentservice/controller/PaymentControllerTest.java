package com.mylearning.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylearning.paymentservice.dto.PaymentRequestDto;
import com.mylearning.paymentservice.dto.PaymentResponseDto;
import com.mylearning.paymentservice.exception.GlobalExceptionHandler;
import com.mylearning.paymentservice.exception.PaymentFailureException;
import com.mylearning.paymentservice.exception.PaymentNotFoundException;
import com.mylearning.paymentservice.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void processPayment_WithValidRequest_ShouldReturnCreated() throws Exception {
        // Arrange
        PaymentRequestDto requestDto = PaymentRequestDto.builder()
                .orderId(1L)
                .userId(1L)
                .amount(100.0)
                .build();

        PaymentResponseDto responseDto = PaymentResponseDto.builder()
                .id(1L)
                .orderId(1L)
                .userId(1L)
                .amount(100.0)
                .status("SUCCESS")
                .paymentDate(LocalDateTime.now())
                .build();

        when(paymentService.processPayment(any(PaymentRequestDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("SUCCESS")));

        verify(paymentService, times(1)).processPayment(any(PaymentRequestDto.class));
    }

    @Test
    void processPayment_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        PaymentRequestDto requestDto = new PaymentRequestDto(); // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.orderId").value("Order ID is required"))
                .andExpect(jsonPath("$.errors.amount").value("Amount must be at least 1"));

        verify(paymentService, never()).processPayment(any());
    }

    @Test
    void processPayment_WhenPaymentFails_ShouldReturnError() throws Exception {
        // Arrange
        PaymentRequestDto requestDto = PaymentRequestDto.builder()
                .orderId(1L)
                .userId(1L)
                .amount(100.0)
                .build();

        when(paymentService.processPayment(any(PaymentRequestDto.class)))
                .thenThrow(new PaymentFailureException("Payment processing failed"));

        // Act & Assert
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.message").value("Payment processing failed"));
    }

    @Test
    void getPaymentById_WhenPaymentExists_ShouldReturnPayment() throws Exception {
        // Arrange
        PaymentResponseDto responseDto = PaymentResponseDto.builder()
                .id(1L)
                .orderId(1L)
                .userId(1L)
                .amount(100.0)
                .status("SUCCESS")
                .paymentDate(LocalDateTime.now())
                .build();

        when(paymentService.getPaymentById(1L)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/payments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("SUCCESS")));

        verify(paymentService, times(1)).getPaymentById(1L);
    }

    @Test
    void getPaymentById_WhenPaymentNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(paymentService.getPaymentById(999L))
                .thenThrow(new PaymentNotFoundException("Payment not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/payments/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message").value("Payment not found with id: 999"));
    }

    @Test
    void getAllPayments_ShouldReturnListOfPayments() throws Exception {
        // Arrange
        PaymentResponseDto payment1 = PaymentResponseDto.builder()
                .id(1L)
                .orderId(1L)
                .userId(1L)
                .amount(100.0)
                .status("SUCCESS")
                .paymentDate(LocalDateTime.now())
                .build();

        PaymentResponseDto payment2 = PaymentResponseDto.builder()
                .id(2L)
                .orderId(2L)
                .userId(2L)
                .amount(200.0)
                .status("SUCCESS")
                .paymentDate(LocalDateTime.now())
                .build();

        when(paymentService.getAllPayments()).thenReturn(Arrays.asList(payment1, payment2));

        // Act & Assert
        mockMvc.perform(get("/api/payments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(paymentService, times(1)).getAllPayments();
    }
}
