package com.example.coffeeshop.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChargeResponse {
    private Long userId;
    private int chargedAmount;
    private int currentBalance;
}
