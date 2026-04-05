package com.example.coffeeshop.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private Long menuId;
    private String menuName;
    private int price;
    private int remainingBalance;
    private LocalDateTime orderedAt;
}