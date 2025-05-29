package com.mylearning.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotBlank(message = "User email is required")
    private String userEmail;

    @NotBlank(message = "Message is required")
    private String message;
}
