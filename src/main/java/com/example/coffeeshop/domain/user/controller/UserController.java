package com.example.coffeeshop.domain.user.controller;

import com.example.coffeeshop.domain.user.dto.ChargeRequest;
import com.example.coffeeshop.domain.user.dto.ChargeResponse;
import com.example.coffeeshop.domain.user.service.UserService;
import com.example.coffeeshop.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/{userId}/points/charge")
    public ApiResponse<ChargeResponse> chargePoint(
            @PathVariable Long userId,
            @RequestBody ChargeRequest request) {
        return ApiResponse.success(userService.chargePoint(userId, request));
    }
}
