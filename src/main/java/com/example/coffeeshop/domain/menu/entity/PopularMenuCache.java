package com.example.coffeeshop.domain.menu.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "popular_menu_cache")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularMenuCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false)
    private int orderCount;

    @Column(nullable = false)
    private LocalDate aggregatedDate;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static PopularMenuCache create(Menu menu, int orderCount) {
        PopularMenuCache cache = new PopularMenuCache();
        cache.menu = menu;
        cache.orderCount = orderCount;
        cache.aggregatedDate = LocalDate.now();
        cache.updatedAt = LocalDateTime.now();
        return cache;
    }

    public void update(int orderCount) {
        this.orderCount = orderCount;
        this.updatedAt = LocalDateTime.now();
    }
}