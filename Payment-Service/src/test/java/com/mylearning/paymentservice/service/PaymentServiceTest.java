package com.mylearning.paymentservice.service;

import com.mylearning.paymentservice.dto.PaymentRequestDto;
import com.mylearning.paymentservice.dto.PaymentResponseDto;
import com.mylearning.paymentservice.dto.UserDto;
import com.mylearning.paymentservice.entity.Payment;
import com.mylearning.paymentservice.exception.PaymentFailureException;
import com.mylearning.paymentservice.exception.PaymentNotFoundException;
import com.mylearning.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentRequestDto paymentRequestDto;
    private Payment payment;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        paymentRequestDto = PaymentRequestDto.builder()
                .orderId(1L)
                .userId(1L)
                .amount(100.0)
                .build();

        payment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .userId(1L)
                .amount(100.0)
                .status("SUCCESS")
                .paymentDate(LocalDateTime.now())
                .build();

        userDto = new UserDto(1L, "testuser", "test@example.com");
    }

    @Test
    void processPayment_WithValidRequest_ShouldReturnPaymentResponse() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(UserDto.class))).thenReturn(userDto);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        PaymentResponseDto response = paymentService.processPayment(paymentRequestDto);

        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(paymentRequestDto.getOrderId(), response.getOrderId());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void processPayment_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(UserDto.class))).thenReturn(null);

        // Act & Assert
        assertThrows(PaymentFailureException.class, 
            () -> paymentService.processPayment(paymentRequestDto));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void getPaymentById_WhenPaymentExists_ShouldReturnPayment() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // Act
        PaymentResponseDto response = paymentService.getPaymentById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(payment.getId(), response.getId());
        verify(paymentRepository, times(1)).findById(1L);
    }

    @Test
    void getPaymentById_WhenPaymentNotExists_ShouldThrowException() {
        // Arrange
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PaymentNotFoundException.class, 
            () -> paymentService.getPaymentById(999L));
    }

    @Test
    void getAllPayments_ShouldReturnListOfPayments() {
        // Arrange
        Payment payment2 = Payment.builder()
                .id(2L)
                .orderId(2L)
                .userId(2L)
                .amount(200.0)
                .status("SUCCESS")
                .paymentDate(LocalDateTime.now())
                .build();
        
        when(paymentRepository.findAll()).thenReturn(Arrays.asList(payment, payment2));

        // Act
        List<PaymentResponseDto> response = paymentService.getAllPayments();

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    void handlePaymentFailure_ShouldReturnFailedPaymentResponse() {
        // Arrange
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        // Act
        PaymentResponseDto response = paymentService.handlePaymentFailure(
            paymentRequestDto, new RuntimeException("Payment failed"));

        // Assert
        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }
}
