package com.mylearning.inventoryservice.controller;

import com.mylearning.inventoryservice.dto.InventoryRequestDto;
import com.mylearning.inventoryservice.dto.InventoryResponseDto;
import com.mylearning.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponseDto> addInventory(@Valid @RequestBody InventoryRequestDto inventory) {
        return ResponseEntity.ok(inventoryService.save(inventory));
    }

    @GetMapping("/isInStock/{productCode}")
    public ResponseEntity<Boolean> isInStock(@PathVariable String productCode) {
        return ResponseEntity.ok(inventoryService.isInStock(productCode));
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponseDto>> getAll() {
        return ResponseEntity.ok(inventoryService.getAll());
    }

    @PutMapping("/{productCode}")
    public ResponseEntity<InventoryResponseDto> updateInventory(@PathVariable String productCode, @Valid @RequestBody InventoryRequestDto inventory) {
        return ResponseEntity.ok(inventoryService.update(productCode, inventory));
    }

    @DeleteMapping("/{productCode}")
    public ResponseEntity<Void> deleteInventory(@PathVariable String productCode) {
        inventoryService.delete(productCode);
        return ResponseEntity.noContent().build();
    }

}
