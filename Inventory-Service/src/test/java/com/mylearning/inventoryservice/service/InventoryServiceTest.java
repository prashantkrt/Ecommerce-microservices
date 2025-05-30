package com.mylearning.inventoryservice.service;

import com.mylearning.inventoryservice.dto.InventoryRequestDto;
import com.mylearning.inventoryservice.dto.InventoryResponseDto;
import com.mylearning.inventoryservice.entity.Inventory;
import com.mylearning.inventoryservice.exception.InventoryNotFoundException;
import com.mylearning.inventoryservice.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Inventory testInventory;
    private InventoryRequestDto testRequestDto;
    private static final String TEST_PRODUCT_CODE = "TEST123";
    private static final String NON_EXISTENT_PRODUCT = "NON_EXISTENT";

    @BeforeEach
    void setUp() {
        testInventory = new Inventory(1L, TEST_PRODUCT_CODE, 10);
        testRequestDto = new InventoryRequestDto(TEST_PRODUCT_CODE, 10);
    }

    @Test
    void isInStock_WhenProductExistsAndInStock_ReturnsTrue() {
        when(inventoryRepository.findByProductCode(TEST_PRODUCT_CODE))
                .thenReturn(Optional.of(testInventory));

        boolean result = inventoryService.isInStock(TEST_PRODUCT_CODE);
        
        assertTrue(result);
        verify(inventoryRepository).findByProductCode(TEST_PRODUCT_CODE);
    }

    @Test
    void isInStock_WhenProductExistsAndOutOfStock_ReturnsFalse() {
        testInventory.setQuantity(0);
        when(inventoryRepository.findByProductCode(TEST_PRODUCT_CODE))
                .thenReturn(Optional.of(testInventory));

        boolean result = inventoryService.isInStock(TEST_PRODUCT_CODE);
        
        assertFalse(result);
    }

    @Test
    void isInStock_WhenProductNotExists_ReturnsFalse() {
        when(inventoryRepository.findByProductCode(NON_EXISTENT_PRODUCT))
                .thenReturn(Optional.empty());

        boolean result = inventoryService.isInStock(NON_EXISTENT_PRODUCT);
        
        assertFalse(result);
    }

    @Test
    void areInStock_WhenProductsExist_ReturnsCorrectStatus() {
        // Arrange
        List<String> productCodes = Arrays.asList("P1", "P2", "P3");
        Inventory inStock1 = new Inventory(1L, "P1", 5);
        Inventory outOfStock = new Inventory(2L, "P2", 0);
        // P3 doesn't exist in the database
        
        when(inventoryRepository.findByProductCodeIn(productCodes))
                .thenReturn(Arrays.asList(inStock1, outOfStock));

        // Act
        List<InventoryResponseDto> result = inventoryService.areInStock(productCodes);

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.get(0).isInStock());  // P1 is in stock
        assertFalse(result.get(1).isInStock()); // P2 is out of stock
        assertFalse(result.get(2).isInStock()); // P3 doesn't exist
        assertEquals(5, result.get(0).getQuantity());
        assertEquals(0, result.get(1).getQuantity());
        assertEquals(0, result.get(2).getQuantity());
    }

    @Test
    void addInventory_WhenProductDoesNotExist_CreatesNewInventory() {
        // Arrange
        when(inventoryRepository.existsByProductCode(TEST_PRODUCT_CODE)).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // Act
        InventoryResponseDto result = inventoryService.addInventory(testRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_PRODUCT_CODE, result.getProductCode());
        assertEquals(10, result.getQuantity());
        verify(inventoryRepository).existsByProductCode(TEST_PRODUCT_CODE);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void addInventory_WhenProductExists_ThrowsException() {
        // Arrange
        when(inventoryRepository.existsByProductCode(TEST_PRODUCT_CODE)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                inventoryService.addInventory(testRequestDto)
        );
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void updateInventory_WhenProductExists_UpdatesInventory() {
        // Arrange
        InventoryRequestDto updateDto = new InventoryRequestDto(TEST_PRODUCT_CODE, 20);
        when(inventoryRepository.findByProductCode(TEST_PRODUCT_CODE))
                .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // Act
        InventoryResponseDto result = inventoryService.updateInventory(TEST_PRODUCT_CODE, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(20, testInventory.getQuantity());
        verify(inventoryRepository).findByProductCode(TEST_PRODUCT_CODE);
        verify(inventoryRepository).save(testInventory);
    }

    @Test
    void updateInventory_WhenProductNotExists_ThrowsException() {
        // Arrange
        when(inventoryRepository.findByProductCode(NON_EXISTENT_PRODUCT))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InventoryNotFoundException.class, () ->
                inventoryService.updateInventory(NON_EXISTENT_PRODUCT, testRequestDto)
        );
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void getAllInventory_WhenInventoryExists_ReturnsAllInventory() {
        // Arrange
        List<Inventory> inventories = Arrays.asList(
            new Inventory(1L, "P1", 10),
            new Inventory(2L, "P2", 5)
        );
        when(inventoryRepository.findAll()).thenReturn(inventories);

        // Act
        List<InventoryResponseDto> result = inventoryService.getAllInventory();

        // Assert
        assertEquals(2, result.size());
        verify(inventoryRepository).findAll();
    }

    @Test
    void getInventoryByProductCode_WhenProductExists_ReturnsInventory() {
        // Arrange
        when(inventoryRepository.findByProductCode(TEST_PRODUCT_CODE))
                .thenReturn(Optional.of(testInventory));

        // Act
        InventoryResponseDto result = inventoryService.getInventoryByProductCode(TEST_PRODUCT_CODE);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_PRODUCT_CODE, result.getProductCode());
        assertEquals(10, result.getQuantity());
        verify(inventoryRepository).findByProductCode(TEST_PRODUCT_CODE);
    }

    @Test
    void getInventoryByProductCode_WhenProductNotExists_ThrowsException() {
        // Arrange
        when(inventoryRepository.findByProductCode(NON_EXISTENT_PRODUCT))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InventoryNotFoundException.class, () ->
                inventoryService.getInventoryByProductCode(NON_EXISTENT_PRODUCT)
        );
    }


    @Test
    void deleteInventory_WhenProductExists_DeletesInventory() {
        // Arrange
        when(inventoryRepository.existsByProductCode(TEST_PRODUCT_CODE)).thenReturn(true);
        doNothing().when(inventoryRepository).deleteByProductCode(TEST_PRODUCT_CODE);

        // Act
        inventoryService.deleteInventory(TEST_PRODUCT_CODE);


        // Assert
        verify(inventoryRepository).existsByProductCode(TEST_PRODUCT_CODE);
        verify(inventoryRepository).deleteByProductCode(TEST_PRODUCT_CODE);
    }

    @Test
    void deleteInventory_WhenProductNotExists_ThrowsException() {
        // Arrange
        when(inventoryRepository.existsByProductCode(NON_EXISTENT_PRODUCT)).thenReturn(false);

        // Act & Assert
        assertThrows(InventoryNotFoundException.class, () ->
                inventoryService.deleteInventory(NON_EXISTENT_PRODUCT)
        );
        verify(inventoryRepository, never()).deleteByProductCode(anyString());
    }

    @Test
    void updateInventoryQuantity_WhenAddingQuantity_UpdatesCorrectly() {
        // Arrange
        when(inventoryRepository.findByProductCode(TEST_PRODUCT_CODE))
                .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // Act
        InventoryResponseDto result = inventoryService.updateInventoryQuantity(TEST_PRODUCT_CODE, 5);

        // Assert
        assertEquals(15, testInventory.getQuantity());
        verify(inventoryRepository).save(testInventory);
    }

    @Test
    void updateInventoryQuantity_WhenSubtractingQuantity_UpdatesCorrectly() {
        // Arrange
        when(inventoryRepository.findByProductCode(TEST_PRODUCT_CODE))
                .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // Act
        InventoryResponseDto result = inventoryService.updateInventoryQuantity(TEST_PRODUCT_CODE, -3);

        // Assert
        assertEquals(7, testInventory.getQuantity());
    }

    @Test
    void updateInventoryQuantity_WhenInsufficientStock_ThrowsException() {
        // Arrange
        when(inventoryRepository.findByProductCode(TEST_PRODUCT_CODE))
                .thenReturn(Optional.of(testInventory));

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                inventoryService.updateInventoryQuantity(TEST_PRODUCT_CODE, -15)
        );
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void getStockLevel_WhenProductExists_ReturnsQuantity() {
        // Arrange
        when(inventoryRepository.findByProductCode(TEST_PRODUCT_CODE))
                .thenReturn(Optional.of(testInventory));

        // Act
        int result = inventoryService.getStockLevel(TEST_PRODUCT_CODE);

        // Assert
        assertEquals(10, result);
    }


    @Test
    void getStockLevel_WhenProductNotExists_ReturnsZero() {
        // Arrange
        when(inventoryRepository.findByProductCode(NON_EXISTENT_PRODUCT))
                .thenReturn(Optional.empty());

        // Act
        int result = inventoryService.getStockLevel(NON_EXISTENT_PRODUCT);

        // Assert
        assertEquals(0, result);
    }
    
    @Test
    void save_WhenProductExists_UpdatesQuantity() {
        // Arrange
        Inventory existingInventory = new Inventory(1L, TEST_PRODUCT_CODE, 5);
        when(inventoryRepository.findByProductCode(TEST_PRODUCT_CODE))
                .thenReturn(Optional.of(existingInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(existingInventory);
        
        InventoryRequestDto requestDto = new InventoryRequestDto(TEST_PRODUCT_CODE, 10);
        
        // Act
        InventoryResponseDto result = inventoryService.save(requestDto);
        
        // Assert
        assertNotNull(result);
        assertEquals(15, existingInventory.getQuantity());
        verify(inventoryRepository).save(existingInventory);
    }
    
    @Test
    void save_WhenProductNotExists_CreatesNewInventory() {
        // Arrange
        when(inventoryRepository.findByProductCode(TEST_PRODUCT_CODE))
                .thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        
        InventoryRequestDto requestDto = new InventoryRequestDto(TEST_PRODUCT_CODE, 10);
        
        // Act
        InventoryResponseDto result = inventoryService.save(requestDto);
        
        // Assert
        assertNotNull(result);
        assertEquals(TEST_PRODUCT_CODE, result.getProductCode());
        assertEquals(10, result.getQuantity());
        verify(inventoryRepository).save(any(Inventory.class));
    }
    
    @Test
    void update_DelegatesToUpdateInventory() {
        // Arrange
        InventoryRequestDto updateDto = new InventoryRequestDto(TEST_PRODUCT_CODE, 20);
        when(inventoryRepository.findByProductCode(TEST_PRODUCT_CODE))
                .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        
        // Act
        InventoryResponseDto result = inventoryService.update(TEST_PRODUCT_CODE, updateDto);
        
        // Assert
        assertNotNull(result);
        assertEquals(20, testInventory.getQuantity());
        verify(inventoryRepository).save(testInventory);
    }
    
    @Test
    void delete_DelegatesToDeleteInventory() {
        // Arrange
        when(inventoryRepository.existsByProductCode(TEST_PRODUCT_CODE)).thenReturn(true);
        doNothing().when(inventoryRepository).deleteByProductCode(TEST_PRODUCT_CODE);
        
        // Act
        inventoryService.delete(TEST_PRODUCT_CODE);
        
        // Assert
        verify(inventoryRepository).existsByProductCode(TEST_PRODUCT_CODE);
        verify(inventoryRepository).deleteByProductCode(TEST_PRODUCT_CODE);
    }
    
    @Test
    void addInventory_WhenQuantityIsZero_WorksCorrectly() {
        // Arrange
        InventoryRequestDto zeroQuantityDto = new InventoryRequestDto("ZERO_QUANTITY", 0);
        when(inventoryRepository.existsByProductCode("ZERO_QUANTITY")).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // Act
        InventoryResponseDto result = inventoryService.addInventory(zeroQuantityDto);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getQuantity());
        assertEquals("ZERO_QUANTITY", result.getProductCode());
    }
    
    @Test
    void updateInventory_WithNegativeQuantity_UpdatesSuccessfully() {
        // Arrange
        InventoryRequestDto negativeQuantityDto = new InventoryRequestDto(TEST_PRODUCT_CODE, -5);
        when(inventoryRepository.findByProductCode(TEST_PRODUCT_CODE))
                .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        
        // Act
        InventoryResponseDto result = inventoryService.updateInventory(TEST_PRODUCT_CODE, negativeQuantityDto);
        
        // Assert - The implementation allows negative quantities
        assertNotNull(result);
        assertEquals(-5, testInventory.getQuantity());
        verify(inventoryRepository).save(testInventory);
    }
    
    @Test
    void areInStock_WithEmptyList_ReturnsEmptyList() {
        // Act
        List<InventoryResponseDto> result = inventoryService.areInStock(List.of());
        
        // Assert
        assertTrue(result.isEmpty());
        // The implementation calls findByProductCodeIn even with empty list
        verify(inventoryRepository).findByProductCodeIn(any());
    }
    
    @Test
    void getStockLevel_WhenProductHasMaxQuantity_ReturnsCorrectValue() {
        // Arrange
        testInventory.setQuantity(Integer.MAX_VALUE);
        when(inventoryRepository.findByProductCode(TEST_PRODUCT_CODE))
                .thenReturn(Optional.of(testInventory));
                
        // Act
        int stockLevel = inventoryService.getStockLevel(TEST_PRODUCT_CODE);
        
        // Assert
        assertEquals(Integer.MAX_VALUE, stockLevel);
    }
    
    @Test
    void updateInventoryQuantity_WithZeroQuantity_UpdatesCorrectly() {
        // Arrange
        when(inventoryRepository.findByProductCode(TEST_PRODUCT_CODE))
                .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        
        // Act
        inventoryService.updateInventoryQuantity(TEST_PRODUCT_CODE, 0);
        
        // Assert - Quantity should remain unchanged
        assertEquals(10, testInventory.getQuantity());
    }
    
    @Test
    void save_WithNullProductCode_ThrowsException() {
        // Arrange
        InventoryRequestDto nullCodeDto = new InventoryRequestDto(null, 10);
        when(inventoryRepository.save(any(Inventory.class))).thenThrow(new DataIntegrityViolationException("Product code cannot be null"));
        
        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> 
            inventoryService.save(nullCodeDto)
        );
    }
}
