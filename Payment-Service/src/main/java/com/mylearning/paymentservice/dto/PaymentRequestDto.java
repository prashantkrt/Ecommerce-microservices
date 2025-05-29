package com.mylearning.paymentservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @Min(value = 1, message = "Amount must be at least 1")
    private double amount;

    @NotNull(message = "Payment status is required")
    private String status;  // e.g., "PAID", "FAILED"
}