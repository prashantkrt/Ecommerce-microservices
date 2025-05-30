package com.mylearning.inventoryservice.service;

import com.mylearning.inventoryservice.dto.InventoryRequestDto;
import com.mylearning.inventoryservice.dto.InventoryResponseDto;

import java.util.List;

/**
 * Service interface for inventory management operations.
 */
public interface InventoryService {

    /**
     * Check if a product is in stock.
     *
     * @param productCode the product code to check
     * @return true if the product is in stock, false otherwise
     */
    boolean isInStock(String productCode);

    /**
     * Check if multiple products are in stock.
     *
     * @param productCodes list of product codes to check
     * @return list of InventoryResponseDto with stock status for each product
     */
    List<InventoryResponseDto> areInStock(List<String> productCodes);

    /**
     * Add new inventory item.
     *
     * @param requestDto the inventory item to add
     * @return the created inventory item
     */
    InventoryResponseDto addInventory(InventoryRequestDto requestDto);

    /**
     * Save or update an inventory item.
     *
     * @param requestDto the inventory data to save
     * @return the saved inventory item
     */
    InventoryResponseDto save(InventoryRequestDto requestDto);

    /**
     * Get all inventory items.
     *
     * @return list of all inventory items
     */
    List<InventoryResponseDto> getAll();
    
    /**
     * Get all inventory items.
     *
     * @return list of all inventory items
     */
    List<InventoryResponseDto> getAllInventory();

    /**
     * Update an existing inventory item.
     *
     * @param productCode the product code of the item to update
     * @param requestDto the updated inventory data
     * @return the updated inventory item
     */
    InventoryResponseDto update(String productCode, InventoryRequestDto requestDto);
    
    /**
     * Update an existing inventory item.
     *
     * @param productCode the product code of the item to update
     * @param requestDto the updated inventory data
     * @return the updated inventory item
     */
    InventoryResponseDto updateInventory(String productCode, InventoryRequestDto requestDto);

    /**
     * Delete an inventory item.
     *
     * @param productCode the product code of the item to delete
     */
    void delete(String productCode);
    
    /**
     * Delete an inventory item.
     *
     * @param productCode the product code of the item to delete
     */
    void deleteInventory(String productCode);

    /**
     * Get inventory by product code.
     *
     * @param productCode the product code to search for
     * @return the inventory item if found
     */
    InventoryResponseDto getInventoryByProductCode(String productCode);

    /**
     * Update inventory quantity (increment or decrement).
     *
     * @param productCode the product code
     * @param quantity the quantity to add (positive) or subtract (negative)
     * @return the updated inventory item
     */
    InventoryResponseDto updateInventoryQuantity(String productCode, int quantity);

    /**
     * Get current stock level for a product.
     *
     * @param productCode the product code
     * @return current stock quantity
     */
    int getStockLevel(String productCode);
}
