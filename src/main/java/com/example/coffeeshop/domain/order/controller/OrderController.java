package com.example.coffeeshop.domain.order.controller;

import com.example.coffeeshop.domain.order.dto.OrderRequest;
import com.example.coffeeshop.domain.order.dto.OrderResponse;
import com.example.coffeeshop.domain.order.service.OrderService;
import com.example.coffeeshop.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> order(@RequestBody OrderRequest request) {
        return ApiResponse.success(orderService.order(request));
    }
}