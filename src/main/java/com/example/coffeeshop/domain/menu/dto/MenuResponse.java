package com.example.coffeeshop.domain.menu.dto;

import com.example.coffeeshop.domain.menu.entity.Menu;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MenuResponse {

    private Long menuId;
    private String name;
    private int price;

    public static MenuResponse from(Menu menu) {
        return new MenuResponse(menu.getId(), menu.getName(), menu.getPrice());
    }
}