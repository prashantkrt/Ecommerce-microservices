package com.mylearning.paymentservice.service;

import com.mylearning.paymentservice.dto.PaymentRequestDto;
import com.mylearning.paymentservice.dto.PaymentResponseDto;
import com.mylearning.paymentservice.event.OrderCreatedEvent;
import com.mylearning.paymentservice.event.PaymentProcessedEvent;

import java.util.List;

public interface PaymentService {
    PaymentResponseDto processPayment(PaymentRequestDto request);
    PaymentResponseDto getPaymentById(Long id);
    List<PaymentResponseDto> getAllPayments();
    
    /**
     * Process payment for an order created event
     * @param event The order created event
     * @return PaymentProcessedEvent with payment details
     */
    PaymentProcessedEvent processOrderCreatedEvent(OrderCreatedEvent event);
}
