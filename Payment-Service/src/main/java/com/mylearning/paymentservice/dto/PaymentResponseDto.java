package com.mylearning.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PaymentResponseDto {
    private Long id;
    private Long orderId;
    private double amount;
    private String status;
    private LocalDateTime paymentDate;
}
