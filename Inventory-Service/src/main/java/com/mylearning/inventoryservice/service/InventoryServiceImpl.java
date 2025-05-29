package com.mylearning.inventoryservice.service;

import com.mylearning.inventoryservice.dto.InventoryRequestDto;
import com.mylearning.inventoryservice.dto.InventoryResponseDto;
import com.mylearning.inventoryservice.entity.Inventory;
import com.mylearning.inventoryservice.exception.InventoryNotFoundException;
import com.mylearning.inventoryservice.repository.InventoryRepository;
import com.mylearning.inventoryservice.util.InventoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;

    @Override
    public InventoryResponseDto save(InventoryRequestDto requestDto) {
        return InventoryMapper.inventoryResponseDto(inventoryRepository.save(InventoryMapper.inventory(requestDto)));
    }

    @Override
    public boolean isInStock(String productCode) {
        return inventoryRepository.findByProductCode(productCode)
                .map(inventory -> inventory.getQuantity() > 0)
                .orElse(false);
    }

    @Override
    public List<InventoryResponseDto> getAll() {
        List<Inventory> inventories = inventoryRepository.findAll();
        return inventories.stream()
                .map(InventoryMapper::inventoryResponseDto)
                .toList();
    }

    @Override
    public InventoryResponseDto update(String productCode, InventoryRequestDto updatedInventory) {
        Inventory existing = inventoryRepository.findByProductCode(productCode)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product code: " + productCode));
        existing.setQuantity(updatedInventory.getQuantity());
        return InventoryMapper.inventoryResponseDto(inventoryRepository.save(existing));
    }

    @Override
    public void delete(String productCode) {
        if (!inventoryRepository.findByProductCode(productCode).isPresent()) {
            throw new InventoryNotFoundException("Cannot delete. Inventory not found for product code: " + productCode);
        }
        inventoryRepository.deleteByProductCode(productCode);
    }

}
