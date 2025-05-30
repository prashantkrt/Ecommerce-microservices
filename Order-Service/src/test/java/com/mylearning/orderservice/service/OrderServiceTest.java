package com.mylearning.orderservice.service;

import com.mylearning.orderservice.dto.*;
import com.mylearning.orderservice.entity.Order;
import com.mylearning.orderservice.exception.OrderNotFoundException;
import com.mylearning.orderservice.exception.OutOfStockException;
import com.mylearning.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderRequestDto orderRequestDto;
    private Order order;
    private ProductDto productDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        orderRequestDto = OrderRequestDto.builder()
                .userId(1L)
                .productCode("P001")
                .quantity(2)
                .build();

        order = Order.builder()
                .id(1L)
                .userId(1L)
                .productCode("P001")
                .quantity(2)
                .orderDate(LocalDateTime.now())
                .build();

        productDto = new ProductDto();
        productDto.setId(1L);
        productDto.setProductCode("P001");
        productDto.setName("Test Product");
        productDto.setPrice(100.0);

        userDto = new UserDto(1L, "testuser", "test@example.com");
    }

    @Test
    void placeOrder_WithValidRequest_ShouldReturnOrderResponse() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(productDto);
        when(restTemplate.getForObject(contains("inventory"), eq(Boolean.class))).thenReturn(true);
        when(restTemplate.getForObject(contains("users"), eq(UserDto.class))).thenReturn(userDto);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderResponseDto response = orderService.placeOrder(orderRequestDto);

        // Assert
        assertNotNull(response);
        assertEquals(orderRequestDto.getProductCode(), response.getProductCode());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void placeOrder_WhenProductNotFound_ShouldThrowException() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(null);

        // Act & Assert
        assertThrows(OrderNotFoundException.class, 
            () -> orderService.placeOrder(orderRequestDto));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrder_WhenOutOfStock_ShouldThrowException() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(productDto);
        when(restTemplate.getForObject(contains("inventory"), eq(Boolean.class))).thenReturn(false);

        // Act & Assert
        assertThrows(OutOfStockException.class, 
            () -> orderService.placeOrder(orderRequestDto));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        OrderResponseDto response = orderService.getOrderById(1L);
        // Assert
        assertNotNull(response);
        assertEquals(order.getId(), response.getId());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void getOrderById_WhenOrderNotExists_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(OrderNotFoundException.class, 
            () -> orderService.getOrderById(999L));
    }

    @Test
    void getAllOrders_ShouldReturnListOfOrders() {
        // Arrange
        Order order2 = Order.builder()
                .id(2L)
                .userId(2L)
                .productCode("P002")
                .quantity(1)
                .orderDate(LocalDateTime.now())
                .build();
        
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order, order2));

        // Act
        List<OrderResponseDto> response = orderService.getAllOrders();

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void placeOrderFallback_ShouldThrowRuntimeException() {
        // Act & Assert
        assertThrows(RuntimeException.class, 
            () -> orderService.placeOrderFallback(orderRequestDto, new RuntimeException("Service unavailable")));
    }
}
