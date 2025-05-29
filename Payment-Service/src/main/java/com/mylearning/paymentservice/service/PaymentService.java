package com.mylearning.paymentservice.service;

import com.mylearning.paymentservice.dto.PaymentRequestDto;
import com.mylearning.paymentservice.dto.PaymentResponseDto;

import java.util.List;

public interface PaymentService {
    public PaymentResponseDto processPayment(PaymentRequestDto request);
    public PaymentResponseDto getPaymentById(Long id);
    public List<PaymentResponseDto> getAllPayments();
}
