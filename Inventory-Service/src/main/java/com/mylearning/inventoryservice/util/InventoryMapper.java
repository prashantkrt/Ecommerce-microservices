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

   public static InventoryResponseDto inventoryResponseDto(Inventory inventory){
        return InventoryResponseDto.builder()
                .id(inventory.getId())
                .productCode(inventory.getProductCode())
                .quantity(inventory.getQuantity())
                .build();
    }
}
