package com.mylearning.inventoryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylearning.inventoryservice.dto.InventoryRequestDto;
import com.mylearning.inventoryservice.dto.InventoryResponseDto;
import com.mylearning.inventoryservice.exception.InventoryNotFoundException;
import com.mylearning.inventoryservice.service.InventoryService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    private final String BASE_URL = "/api/inventory";
    private final String TEST_PRODUCT_CODE = "TEST123";
    private InventoryRequestDto testRequestDto;
    private InventoryResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController).build();
        testRequestDto = new InventoryRequestDto(TEST_PRODUCT_CODE, 10);
        testResponseDto = new InventoryResponseDto(1L, TEST_PRODUCT_CODE, 10, true);
    }

    @Test
    void addInventory_ShouldReturnCreatedInventory() throws Exception {
        when(inventoryService.save(any(InventoryRequestDto.class))).thenReturn(testResponseDto);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productCode", is(TEST_PRODUCT_CODE)))
                .andExpect(jsonPath("$.quantity", is(10)));

        verify(inventoryService, times(1)).save(any(InventoryRequestDto.class));
    }

    @Test
    void isInStock_WhenProductExists_ReturnsStatus() throws Exception {
        when(inventoryService.isInStock(TEST_PRODUCT_CODE)).thenReturn(true);

        mockMvc.perform(get(BASE_URL + "/isInStock/" + TEST_PRODUCT_CODE))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(inventoryService, times(1)).isInStock(TEST_PRODUCT_CODE);
    }

    @Test
    void getAll_ShouldReturnAllInventories() throws Exception {
        List<InventoryResponseDto> inventories = Arrays.asList(
            new InventoryResponseDto(1L, "P1", 10, true),
            new InventoryResponseDto(2L, "P2", 0, false)
        );
        
        when(inventoryService.getAll()).thenReturn(inventories);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productCode", is("P1")))
                .andExpect(jsonPath("$[1].productCode", is("P2")));

        verify(inventoryService, times(1)).getAll();
    }

    @Test
    void updateInventory_WhenProductExists_UpdatesAndReturnsInventory() throws Exception {
        InventoryRequestDto updateDto = new InventoryRequestDto(TEST_PRODUCT_CODE, 20);
        InventoryResponseDto updatedResponse = new InventoryResponseDto(1L, TEST_PRODUCT_CODE, 20, true);
        
        when(inventoryService.update(eq(TEST_PRODUCT_CODE), any(InventoryRequestDto.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put(BASE_URL + "/" + TEST_PRODUCT_CODE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productCode", is(TEST_PRODUCT_CODE)))
                .andExpect(jsonPath("$.quantity", is(20)));

        verify(inventoryService, times(1)).update(eq(TEST_PRODUCT_CODE), any(InventoryRequestDto.class));
    }

    @Test
    void updateInventory_WhenProductNotExists_ReturnsNotFound() throws Exception {
        when(inventoryService.update(eq("NON_EXISTENT"), any(InventoryRequestDto.class)))
                .thenThrow(new InventoryNotFoundException("Inventory not found"));

        mockMvc.perform(put(BASE_URL + "/NON_EXISTENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteInventory_WhenProductExists_DeletesAndReturnsNoContent() throws Exception {
        doNothing().when(inventoryService).delete(TEST_PRODUCT_CODE);

        mockMvc.perform(delete(BASE_URL + "/" + TEST_PRODUCT_CODE))
                .andExpect(status().isNoContent());

        verify(inventoryService, times(1)).delete(TEST_PRODUCT_CODE);
    }

    @Test
    void deleteInventory_WhenProductNotExists_ReturnsNotFound() throws Exception {
        doThrow(new InventoryNotFoundException("Inventory not found"))
                .when(inventoryService).delete("NON_EXISTENT");

        mockMvc.perform(delete(BASE_URL + "/NON_EXISTENT"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addInventory_WithInvalidInput_ReturnsBadRequest() throws Exception {
        InventoryRequestDto invalidDto = new InventoryRequestDto("", -5);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(inventoryService, never()).save(any(InventoryRequestDto.class));
    }
}
