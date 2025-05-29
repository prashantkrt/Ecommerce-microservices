package com.mylearning.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {

    @NotBlank(message = "Product code is required")
    private String productCode;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}