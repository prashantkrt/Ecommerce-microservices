package com.mylearning.orderservice.service;

import com.mylearning.orderservice.dto.OrderRequestDto;
import com.mylearning.orderservice.dto.OrderResponseDto;
import com.mylearning.orderservice.entity.Order;
import com.mylearning.orderservice.exception.OrderNotFoundException;
import com.mylearning.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public OrderResponseDto placeOrder(OrderRequestDto requestDto) {
        Order order = Order.builder()
                .productCode(requestDto.getProductCode())
                .quantity(requestDto.getQuantity())
                .orderDate(LocalDateTime.now())
                .build();
        Order saved = orderRepository.save(order);
        return mapToDto(saved);
    }

    @Override
    public List<OrderResponseDto> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDto getOrderById(Long id) {
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
