package com.mylearning.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class InventoryResponseDto {
    private Long id;
    private String productCode;
    private int quantity;
}
