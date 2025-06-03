package com.mylearning.paymentservice.service;

import com.mylearning.paymentservice.dto.PaymentRequestDto;
import com.mylearning.paymentservice.dto.PaymentResponseDto;
import com.mylearning.paymentservice.dto.UserDto;
import com.mylearning.paymentservice.event.OrderCreatedEvent;
import com.mylearning.paymentservice.event.PaymentProcessedEvent;
import com.mylearning.paymentservice.entity.Payment;
import com.mylearning.paymentservice.exception.PaymentFailureException;
import com.mylearning.paymentservice.exception.PaymentNotFoundException;
import com.mylearning.paymentservice.repository.PaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;

    @Override
    @CircuitBreaker(name = "paymentService", fallbackMethod = "handlePaymentFailure")
    @Retry(name = "paymentService")
    @Transactional(rollbackFor = Exception.class, timeout = 30, propagation = Propagation.REQUIRED)
    public PaymentResponseDto processPayment(PaymentRequestDto request) {
        // Validate user by calling user-service
        String userServiceUrl = "http://user-service/api/users/" + request.getUserId();

        UserDto user = restTemplate.getForObject(userServiceUrl, UserDto.class);

        if (user == null) {
            throw new PaymentFailureException("User not found");
        }

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .status("SUCCESS")
                .userId(request.getUserId())
                .paymentDate(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true, timeout = 10)
    public PaymentResponseDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));
        return mapToDto(payment);
    }


    @Override
    @Transactional(readOnly = true, timeout = 15)
    public List<PaymentResponseDto> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


    private PaymentResponseDto mapToDto(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .build();
    }


    public PaymentResponseDto handlePaymentFailure(PaymentRequestDto request, Throwable throwable) {
        log.error(" Payment processing failed for orderId={} due to: {}", request.getOrderId(), throwable.getMessage());

        // Optional: Save failed payment attempt with status FAILED
        Payment failedPayment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .status("FAILED")
                .userId(request.getUserId())
                .paymentDate(LocalDateTime.now())
                .build();

        paymentRepository.save(failedPayment);

        // Return a response indicating failure
        return PaymentResponseDto.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .status("FAILED")
                .build();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30, propagation = Propagation.REQUIRED)
    public PaymentProcessedEvent processOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Processing payment for order: {}", event.getOrderId());
        
        try {
            // Simulate payment processing
            // In a real scenario, this would integrate with a payment gateway
            Thread.sleep(1000); // Simulate processing time
            
            // Create and save payment record
            Payment payment = Payment.builder()
                    .orderId(Long.parseLong(event.getOrderId()))
                    .amount(event.getAmount().doubleValue())
                    .status("COMPLETED")
                    .userId(Long.parseLong(event.getUserId()))
                    .paymentDate(LocalDateTime.now())
                    .build();
            
            payment = paymentRepository.save(payment);
            
            log.info("Successfully processed payment for order: {}", event.getOrderId());
            
            // Return payment processed event
            return PaymentProcessedEvent.builder()
                    .paymentId(String.valueOf(payment.getId()))
                    .orderId(String.valueOf(payment.getOrderId()))
                    .userId(String.valueOf(payment.getUserId()))
                    .amount(BigDecimal.valueOf(payment.getAmount()))
                    .status("COMPLETED")
                    .transactionId("TXN" + System.currentTimeMillis())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error processing payment for order: {}", event.getOrderId(), e);
            throw new PaymentFailureException("Payment processing failed: " + e.getMessage());
        }
    }
}
