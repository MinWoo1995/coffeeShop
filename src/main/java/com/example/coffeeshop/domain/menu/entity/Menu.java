package com.example.coffeeshop.domain.menu.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "menus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private boolean isAvailable;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static Menu createForTest(String name, int price) {
        Menu menu = new Menu();
        menu.name = name;
        menu.price = price;
        menu.isAvailable = true;
        return menu;
    }
}