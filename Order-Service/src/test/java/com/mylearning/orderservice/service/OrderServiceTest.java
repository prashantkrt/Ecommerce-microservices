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
import org.springframework.test.util.ReflectionTestUtils;
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
        // Set up service URLs
        ReflectionTestUtils.setField(orderService, "productServiceUrl", "http://product-service/api/products");
        ReflectionTestUtils.setField(orderService, "inventoryServiceUrl", "http://inventory-service/api/inventory");
        ReflectionTestUtils.setField(orderService, "paymentServiceUrl", "http://payment-service/api/payments");
        ReflectionTestUtils.setField(orderService, "userServiceUrl", "http://user-service/api/users");
        ReflectionTestUtils.setField(orderService, "notificationServiceUrl", "http://notification-service/api/notifications");

        orderRequestDto = OrderRequestDto.builder()
                .userId(1L)
                .productCode("P001")
                .quantity(2)
                .build();

        order = Order.builder()
                .id(1L)
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
        when(restTemplate.getForObject(contains("/code/P001"), eq(ProductDto.class))).thenReturn(productDto);
        when(restTemplate.getForObject(contains("isInStock/P001"), eq(Boolean.class))).thenReturn(true);
        when(restTemplate.getForObject(contains("users/1"), eq(UserDto.class))).thenReturn(userDto);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });

        // Act
        OrderResponseDto response = orderService.placeOrder(orderRequestDto);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(orderRequestDto.getProductCode(), response.getProductCode());
        assertEquals(orderRequestDto.getQuantity(), response.getQuantity());
        assertNotNull(response.getOrderDate());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(restTemplate, times(1)).postForEntity(contains("payments/process"), any(), eq(Void.class));
        verify(restTemplate, times(1)).postForEntity(contains("notifications/send"), any(), eq(Void.class));
    }

    @Test
    void placeOrder_WhenProductNotFound_ShouldThrowException() {
        // Arrange
        when(restTemplate.getForObject(contains("/code/P001"), eq(ProductDto.class))).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.placeOrder(orderRequestDto));
        assertEquals("Product not found with code: P001", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrder_WhenOutOfStock_ShouldThrowException() {
        // Arrange
        when(restTemplate.getForObject(contains("/code/P001"), eq(ProductDto.class))).thenReturn(productDto);
        when(restTemplate.getForObject(contains("isInStock/P001"), eq(Boolean.class))).thenReturn(false);

        // Act & Assert
        OutOfStockException exception = assertThrows(OutOfStockException.class, 
            () -> orderService.placeOrder(orderRequestDto));
        assertEquals("Product is out of stock", exception.getMessage());
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
        assertEquals(order.getProductCode(), response.getProductCode());
        assertEquals(order.getQuantity(), response.getQuantity());
        assertEquals(order.getOrderDate(), response.getOrderDate());
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
        assertEquals(order.getId(), response.get(0).getId());
        assertEquals(order2.getId(), response.get(1).getId());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void placeOrderFallback_ShouldThrowRuntimeException() {
        // Act & Assert
        assertThrows(RuntimeException.class, 
            () -> orderService.placeOrderFallback(orderRequestDto, new RuntimeException("Service unavailable")));
    }
}
