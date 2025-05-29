package com.mylearning.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InventoryRequestDto {

    @NotBlank(message = "Product code is required")
    private String productCode;

    @Min(value = 0, message = "Quantity must be at least 0")
    private int quantity;
}
