package com.mylearning.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylearning.orderservice.dto.OrderRequestDto;
import com.mylearning.orderservice.dto.OrderResponseDto;
import com.mylearning.orderservice.exception.GlobalExceptionHandler;
import com.mylearning.orderservice.exception.OrderNotFoundException;
import com.mylearning.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private OrderResponseDto orderResponseDto;

    private OrderResponseDto createSampleOrderResponse(Long id) {
        return OrderResponseDto.builder()
                .id(id)
                .userId(id)
                .productCode("P" + String.format("%03d", id))
                .quantity(2)
                .orderDate(LocalDateTime.now())
                .build();
    }

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        orderResponseDto = createSampleOrderResponse(1L);
    }

    @Test
    void placeOrder_WithValidRequest_ShouldReturnCreated() throws Exception {
        // Arrange
        OrderRequestDto requestDto = new OrderRequestDto(1L, "P001", 2);
        when(orderService.placeOrder(any(OrderRequestDto.class))).thenReturn(orderResponseDto);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.productCode").value("P001"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.userId").value(1L));

        verify(orderService, times(1)).placeOrder(any(OrderRequestDto.class));
    }

    @Test
    void placeOrder_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        OrderRequestDto requestDto = new OrderRequestDto(); // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isMap())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(orderService, never()).placeOrder(any(OrderRequestDto.class));
    }

    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() throws Exception {
        // Arrange
        when(orderService.getOrderById(1L)).thenReturn(orderResponseDto);

        // Act & Assert
        mockMvc.perform(get("/api/orders/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.productCode").value("P001"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.userId").value(1L));

        verify(orderService, times(1)).getOrderById(1L);
    }

    @Test
    void getOrderById_WhenOrderNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long nonExistentId = 999L;
        when(orderService.getOrderById(nonExistentId))
                .thenThrow(new OrderNotFoundException("Order not found with id: " + nonExistentId));

        // Act & Assert
        mockMvc.perform(get("/api/orders/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Order not found with id: " + nonExistentId)))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(orderService, times(1)).getOrderById(nonExistentId);
    }

    @Test
    void getAllOrders_ShouldReturnListOfOrders() throws Exception {
        // Arrange
        OrderResponseDto order2 = createSampleOrderResponse(2L);
        order2.setQuantity(1);

        List<OrderResponseDto> orders = Arrays.asList(orderResponseDto, order2);
        when(orderService.getAllOrders()).thenReturn(orders);

        // Act & Assert
        mockMvc.perform(get("/api/orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].productCode").value("P001"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].productCode").value("P002"));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void placeOrder_WhenServiceUnavailable_ShouldReturnError() throws Exception {
        // Arrange
        OrderRequestDto requestDto = new OrderRequestDto(1L, "P001", 2);
        when(orderService.placeOrder(any(OrderRequestDto.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Service unavailable"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
