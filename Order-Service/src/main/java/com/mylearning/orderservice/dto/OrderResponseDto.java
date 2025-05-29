package com.mylearning.orderservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private Long id;
    private String productCode;
    private int quantity;
    private LocalDateTime orderDate;
}
