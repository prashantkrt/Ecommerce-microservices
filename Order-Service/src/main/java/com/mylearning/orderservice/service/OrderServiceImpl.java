package com.mylearning.orderservice.service;

import com.mylearning.orderservice.dto.*;
import com.mylearning.orderservice.entity.Order;
import com.mylearning.orderservice.exception.OrderNotFoundException;
import com.mylearning.orderservice.exception.OutOfStockException;
import com.mylearning.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    @Value("${product.service.url:http://product-service/api/products}")
    private String productServiceUrl;

    @Value("${inventory.service.url:http://inventory-service/api/inventory}")
    private String inventoryServiceUrl;

    @Value("${payment.service.url:http://payment-service/api/payments}")
    private String paymentServiceUrl;

    @Value("${user.service.url:http://user-service/api/users}")
    private String userServiceUrl;

    @Value("${notification.service.url:http://notification-service/api/notifications}")
    private String notificationServiceUrl;

    @Override
    public OrderResponseDto placeOrder(OrderRequestDto requestDto) {

        log.info("Start placing order for productCode={} by userId={}", requestDto.getProductCode(), requestDto.getUserId());

        // Validate product
        log.info("Validating product for productCode={}", requestDto.getProductCode());
        ProductDto product = restTemplate.getForObject(productServiceUrl + "/code/" + requestDto.getProductCode(), ProductDto.class);
        if (product == null) {
            log.error("Product not found for productCode={}", requestDto.getProductCode());
            throw new RuntimeException("Product not found with code: " + requestDto.getProductCode());
        }
        log.info("Product found: {}", product.getName());

        // Check inventory
        log.info("Checking inventory stock for productCode={}", requestDto.getProductCode());
        Boolean isInStock = restTemplate.getForObject(inventoryServiceUrl + "/isInStock/" + requestDto.getProductCode(), Boolean.class);
        if (Boolean.FALSE.equals(isInStock)) {
            log.warn("Product out of stock for productCode={}", requestDto.getProductCode());
            throw new OutOfStockException("Product is out of stock");
        }
        log.info("Product is in stock");

        // Save order
        Order order = Order.builder()
                .productCode(requestDto.getProductCode())
                .quantity(requestDto.getQuantity())
                .orderDate(LocalDateTime.now())
                .build();
        Order saved = orderRepository.save(order);
        log.info("Order saved with id={}", saved.getId());

        // Map response with userId included
        OrderResponseDto responseDto = mapToDto(saved);
        responseDto.setUserId(requestDto.getUserId());

        // Process payment
        log.info("Processing payment for orderId={} userId={}", saved.getId(), requestDto.getUserId());
        PaymentRequest paymentRequest = new PaymentRequest(saved.getId(), requestDto.getUserId(), product.getPrice() * requestDto.getQuantity());
        restTemplate.postForEntity(paymentServiceUrl + "/process", paymentRequest, Void.class);
        log.info("Payment processed for orderId={}", saved.getId());

        // Fetch user info for notification
        log.info("Fetching user info for userId={}", requestDto.getUserId());
        UserDto user = restTemplate.getForObject(userServiceUrl + "/" + requestDto.getUserId(), UserDto.class);
        if (user == null) {
            log.warn("User not found with id={}, skipping notification", requestDto.getUserId());
        } else {

            NotificationRequest notificationRequest = new NotificationRequest(
                    saved.getId(),
                    requestDto.getUserId(),
                    user.getEmail(),
                    "Order placed for product: " + product.getName()
            );
            log.info("Sending notification to userEmail={}", user.getEmail());
            restTemplate.postForEntity(notificationServiceUrl + "/send", notificationRequest, Void.class);
            log.info("Notification sent for orderId={}", saved.getId());
        }

        log.info("Order process completed for orderId={}", saved.getId());
        return responseDto;
    }

    @Override
    public List<OrderResponseDto> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll()
                .stream()
                .map(order -> {
                    OrderResponseDto dto = mapToDto(order);
                    log.debug("Found order: {}", dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDto getOrderById(Long id) {
        log.info("Fetching order by id={}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        return mapToDto(order);
    }

    private OrderResponseDto mapToDto(Order order) {
        return OrderResponseDto.builder()
                .id(order.getId())
                .productCode(order.getProductCode())
                .quantity(order.getQuantity())
                .orderDate(order.getOrderDate())
                .build();
    }
}
