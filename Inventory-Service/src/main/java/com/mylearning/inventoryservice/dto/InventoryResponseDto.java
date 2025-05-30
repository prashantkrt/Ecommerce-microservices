package com.mylearning.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for inventory responses.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class InventoryResponseDto {
    private Long id;
    private String productCode;
    private int quantity;
    private boolean inStock;

    /**
     * Checks if the product is in stock.
     * @return true if quantity is greater than 0, false otherwise
     */
    public boolean isInStock() {
        return quantity > 0;
    }
}
