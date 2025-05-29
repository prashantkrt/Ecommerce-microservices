package com.mylearning.inventoryservice.service;

import com.mylearning.inventoryservice.dto.InventoryRequestDto;
import com.mylearning.inventoryservice.dto.InventoryResponseDto;
import com.mylearning.inventoryservice.entity.Inventory;

import java.util.List;

public interface InventoryService {

    boolean isInStock(String productCode);
    public InventoryResponseDto save(InventoryRequestDto requestDto);
    public List<InventoryResponseDto> getAll();
    public InventoryResponseDto update(String productCode, InventoryRequestDto updatedInventory);
    public void delete(String productCode);
}
