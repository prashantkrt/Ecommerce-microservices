package com.mylearning.orderservice.service;

import com.mylearning.orderservice.dto.OrderRequestDto;
import com.mylearning.orderservice.dto.OrderResponseDto;

import java.util.List;

public interface OrderService {
    public OrderResponseDto placeOrder(OrderRequestDto requestDto);
    public List<OrderResponseDto> getAllOrders();
    public OrderResponseDto getOrderById(Long id);
}
