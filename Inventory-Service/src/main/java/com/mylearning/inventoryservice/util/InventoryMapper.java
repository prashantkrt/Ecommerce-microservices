package com.mylearning.inventoryservice.util;

import com.mylearning.inventoryservice.dto.InventoryRequestDto;
import com.mylearning.inventoryservice.dto.InventoryResponseDto;
import com.mylearning.inventoryservice.entity.Inventory;

public class InventoryMapper {
    public static Inventory inventory(InventoryRequestDto requestDto){
        return Inventory.builder()
                .productCode(requestDto.getProductCode())
                .quantity(requestDto.getQuantity())
                .build();

    }

   /**
     * Maps an Inventory entity to an InventoryResponseDto.
     *
     * @param inventory the inventory entity to map
     * @return the mapped InventoryResponseDto
     */
    public static InventoryResponseDto inventoryResponseDto(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        return InventoryResponseDto.builder()
                .id(inventory.getId())
                .productCode(inventory.getProductCode())
                .quantity(inventory.getQuantity())
                .inStock(inventory.getQuantity() > 0)
                .build();
    }
}
