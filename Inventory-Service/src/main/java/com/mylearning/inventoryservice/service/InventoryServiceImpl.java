package com.mylearning.inventoryservice.service;

import com.mylearning.inventoryservice.dto.InventoryRequestDto;
import com.mylearning.inventoryservice.dto.InventoryResponseDto;
import com.mylearning.inventoryservice.entity.Inventory;
import com.mylearning.inventoryservice.exception.InventoryNotFoundException;
import com.mylearning.inventoryservice.repository.InventoryRepository;
import com.mylearning.inventoryservice.util.InventoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of IInventory interface for inventory management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, timeout = 10)
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30, propagation = Propagation.REQUIRED)
    public InventoryResponseDto save(InventoryRequestDto requestDto) {
        log.info("Saving inventory item for product: {}", requestDto.getProductCode());
        Inventory inventory = inventoryRepository.findByProductCode(requestDto.getProductCode())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + requestDto.getQuantity());
                    return existing;
                })
                .orElseGet(() -> Inventory.builder()
                        .productCode(requestDto.getProductCode())
                        .quantity(requestDto.getQuantity())
                        .build());
                        
        return InventoryMapper.inventoryResponseDto(inventoryRepository.save(inventory));
    }
    
    @Override
    public List<InventoryResponseDto> getAll() {
        return getAllInventory();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 15, propagation = Propagation.REQUIRED)
    public void delete(String productCode) {
        deleteInventory(productCode);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30, propagation = Propagation.REQUIRED)
    public InventoryResponseDto update(String productCode, InventoryRequestDto requestDto) {
        return updateInventory(productCode, requestDto);
    }
    
    @Override
    public boolean isInStock(String productCode) {
        log.debug("Checking stock for product: {}", productCode);
        return inventoryRepository.findByProductCode(productCode)
                .map(inventory -> inventory.getQuantity() > 0)
                .orElse(false);
    }

    @Override
    public List<InventoryResponseDto> areInStock(List<String> productCodes) {
        log.debug("Checking stock for products: {}", productCodes);
        List<Inventory> inventories = inventoryRepository.findByProductCodeIn(productCodes);
        
        return productCodes.stream()
                .map(code -> {
                    Inventory inventory = inventories.stream()
                            .filter(i -> i.getProductCode().equals(code))
                            .findFirst()
                            .orElse(null);
                    
                    boolean inStock = inventory != null && inventory.getQuantity() > 0;
                    return InventoryResponseDto.builder()
                            .productCode(code)
                            .inStock(inStock)
                            .quantity(inventory != null ? inventory.getQuantity() : 0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30, propagation = Propagation.REQUIRED)
    public InventoryResponseDto addInventory(InventoryRequestDto requestDto) {
        log.info("Adding new inventory item for product: {}", requestDto.getProductCode());
        if (inventoryRepository.existsByProductCode(requestDto.getProductCode())) {
            throw new IllegalStateException("Inventory already exists for product: " + requestDto.getProductCode());
        }
        
        Inventory inventory = Inventory.builder()
                .productCode(requestDto.getProductCode())
                .quantity(requestDto.getQuantity())
                .build();
                
        return InventoryMapper.inventoryResponseDto(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30, propagation = Propagation.REQUIRED)
    public InventoryResponseDto updateInventory(String productCode, InventoryRequestDto requestDto) {
        log.info("Updating inventory for product: {}", productCode);
        Inventory inventory = inventoryRepository.findByProductCode(productCode)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product code: " + productCode));
                
        inventory.setQuantity(requestDto.getQuantity());
        return InventoryMapper.inventoryResponseDto(inventoryRepository.save(inventory));
    }

    @Override
    public List<InventoryResponseDto> getAllInventory() {
        log.debug("Fetching all inventory items");
        return inventoryRepository.findAll().stream()
                .map(InventoryMapper::inventoryResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public InventoryResponseDto getInventoryByProductCode(String productCode) {
        log.debug("Fetching inventory for product: {}", productCode);
        return inventoryRepository.findByProductCode(productCode)
                .map(InventoryMapper::inventoryResponseDto)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product code: " + productCode));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 15, propagation = Propagation.REQUIRED)
    public void deleteInventory(String productCode) {
        log.info("Deleting inventory for product: {}", productCode);
        if (!inventoryRepository.existsByProductCode(productCode)) {
            throw new InventoryNotFoundException("Cannot delete. Inventory not found for product code: " + productCode);
        }
        inventoryRepository.deleteByProductCode(productCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30, propagation = Propagation.REQUIRED)
    public InventoryResponseDto updateInventoryQuantity(String productCode, int quantity) {
        log.info("Updating quantity by {} for product: {}", quantity, productCode);
        Inventory inventory = inventoryRepository.findByProductCode(productCode)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product code: " + productCode));
                
        int newQuantity = inventory.getQuantity() + quantity;
        if (newQuantity < 0) {
            throw new IllegalStateException("Insufficient stock for product: " + productCode);
        }
        
        inventory.setQuantity(newQuantity);
        return InventoryMapper.inventoryResponseDto(inventoryRepository.save(inventory));
    }

    @Override
    public int getStockLevel(String productCode) {
        log.debug("Getting stock level for product: {}", productCode);
        return inventoryRepository.findByProductCode(productCode)
                .map(Inventory::getQuantity)
                .orElse(0);
    }
}
