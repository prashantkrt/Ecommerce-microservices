package com.mylearning.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylearning.productservice.dto.ProductRequestDto;
import com.mylearning.productservice.dto.ProductResponseDto;
import com.mylearning.productservice.exception.ProductNotFoundException;
import com.mylearning.productservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ProductResponseDto productResponseDto;
    private ProductRequestDto productRequestDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice()
                .build();

        productResponseDto = ProductResponseDto.builder()
                .id(1L)
                .productCode("P001")
                .name("Test Product")
                .description("Test Description")
                .price(99.99)
                .build();

        productRequestDto = ProductRequestDto.builder()
                .productCode("P001")
                .name("Test Product")
                .description("Test Description")
                .price(99.99)
                .build();
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        when(productService.create(any(ProductRequestDto.class))).thenReturn(productResponseDto);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(productResponseDto.getName())))
                .andExpect(jsonPath("$.description", is(productResponseDto.getDescription())));

        verify(productService, times(1)).create(any(ProductRequestDto.class));
    }

    @Test
    void getAllProducts_ShouldReturnListOfProducts() throws Exception {
        List<ProductResponseDto> productList = Arrays.asList(productResponseDto);
        when(productService.getAll()).thenReturn(productList);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(productResponseDto.getName())));

        verify(productService, times(1)).getAll();
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() throws Exception {
        when(productService.getById(1L)).thenReturn(productResponseDto);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(productResponseDto.getName())));

        verify(productService, times(1)).getById(1L);
    }

    @Test
    void getProductById_WhenProductNotExists_ShouldReturnNotFound() throws Exception {
        when(productService.getById(999L)).thenThrow(new ProductNotFoundException("Product not found with ID: 999"));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Product not found with ID: 999")));

        verify(productService, times(1)).getById(999L);
    }

    @Test
    void getProductByProductCode_WhenProductExists_ShouldReturnProduct() throws Exception {
        when(productService.getByProductCode("P001")).thenReturn(productResponseDto);

        mockMvc.perform(get("/api/products/code/P001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productCode", is("P001")))
                .andExpect(jsonPath("$.name", is(productResponseDto.getName())));

        verify(productService, times(1)).getByProductCode("P001");
    }

    @Test
    void createProduct_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        ProductRequestDto invalidProduct = new ProductRequestDto();
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isMap());

        verify(productService, never()).create(any(ProductRequestDto.class));
    }
}
