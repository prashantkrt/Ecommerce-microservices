package com.mylearning.productservice.service;

import com.mylearning.productservice.dto.ProductRequestDto;
import com.mylearning.productservice.dto.ProductResponseDto;
import com.mylearning.productservice.entity.Product;
import com.mylearning.productservice.exception.ProductNotFoundException;
import com.mylearning.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequestDto productRequestDto;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .productCode("P001")
                .name("Test Product")
                .description("Test Description")
                .price(99.99)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productRequestDto = ProductRequestDto.builder()
                .productCode("P001")
                .name("Test Product")
                .description("Test Description")
                .price(99.99)
                .build();
    }

    @Test
    void createProduct_ShouldReturnProductResponse() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        ProductResponseDto response = productService.create(productRequestDto);

        // Assert
        assertNotNull(response);
        assertEquals(product.getName(), response.getName());
        assertEquals(product.getDescription(), response.getDescription());
        assertEquals(product.getPrice(), response.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void getAllProducts_ShouldReturnListOfProducts() {
        // Arrange
        List<Product> products = Arrays.asList(product);
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<ProductResponseDto> response = productService.getAll();
        
        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
        assertEquals(product.getName(), response.get(0).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        ProductResponseDto response = productService.getById(1L);
        
        // Assert
        assertNotNull(response);
        assertEquals(product.getId(), response.getId());
        assertEquals(product.getName(), response.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_WhenProductNotExists_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> productService.getById(999L));
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void getProductByProductCode_WhenProductExists_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findByProductCode("P001")).thenReturn(Optional.of(product));

        // Act
        ProductResponseDto response = productService.getByProductCode("P001");
        
        // Assert
        assertNotNull(response);
        assertEquals(product.getProductCode(), response.getProductCode());
        verify(productRepository, times(1)).findByProductCode("P001");
    }

    @Test
    void getProductByProductCode_WhenProductNotExists_ShouldThrowException() {
        // Arrange
        when(productRepository.findByProductCode("INVALID")).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> productService.getByProductCode("INVALID"));
        verify(productRepository, times(1)).findByProductCode("INVALID");
    }
}
