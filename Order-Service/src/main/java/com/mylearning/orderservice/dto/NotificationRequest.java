package com.mylearning.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationRequest {
    private Long orderId;
    private Long userId;
    private String userEmail;
    private String message;
}
