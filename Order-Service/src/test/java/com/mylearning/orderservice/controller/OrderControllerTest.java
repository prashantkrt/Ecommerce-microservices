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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private OrderResponseDto orderResponseDto;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        orderResponseDto = OrderResponseDto.builder()
                .id(1L)
                .userId(1L)
                .productCode("P001")
                .quantity(2)
                .orderDate(LocalDateTime.now())
                .build();
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
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.productCode", is("P001")));

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
                .andExpect(jsonPath("$.status", is(400)));

        verify(orderService, never()).placeOrder(any());
    }

    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() throws Exception {
        // Arrange
        when(orderService.getOrderById(1L)).thenReturn(orderResponseDto);

        // Act & Assert
        mockMvc.perform(get("/api/orders/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.productCode", is("P001")));

        verify(orderService, times(1)).getOrderById(1L);
    }

    @Test
    void getOrderById_WhenOrderNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(orderService.getOrderById(999L))
                .thenThrow(new OrderNotFoundException("Order not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/orders/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void getAllOrders_ShouldReturnListOfOrders() throws Exception {
        // Arrange
        OrderResponseDto order2 = OrderResponseDto.builder()
                .id(2L)
                .userId(2L)
                .productCode("P002")
                .quantity(1)
                .orderDate(LocalDateTime.now())
                .build();

        List<OrderResponseDto> orders = Arrays.asList(orderResponseDto, order2);
        when(orderService.getAllOrders()).thenReturn(orders);

        // Act & Assert
        mockMvc.perform(get("/api/orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));

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
                .andExpect(jsonPath("$.status", is(500)));
    }
}
