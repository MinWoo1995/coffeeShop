package com.example.coffeeshop.domain.menu.controller;

import com.example.coffeeshop.domain.menu.dto.MenuResponse;
import com.example.coffeeshop.domain.menu.service.MenuService;
import com.example.coffeeshop.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ApiResponse<List<MenuResponse>> getMenus() {
        return ApiResponse.success(menuService.getMenus());
    }
}
