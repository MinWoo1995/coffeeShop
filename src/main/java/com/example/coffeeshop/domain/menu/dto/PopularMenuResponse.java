package com.example.coffeeshop.domain.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PopularMenuResponse {
    private List<PopularMenuItem> popularMenus;
    private LocalDateTime aggregatedAt;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PopularMenuItem {
        private int rank;
        private Long menuId;
        private String name;
        private int price;
        private int orderCount;
    }
}