package com.example.coffeeshop.domain.user.entity;

import com.example.coffeeshop.global.exception.CustomException;
import com.example.coffeeshop.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int pointBalance;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static User create(String name) {
        User user = new User();
        user.name = name;
        user.pointBalance = 0;
        return user;
    }

    public void chargePoint(int amount) {
        if (amount <= 0) {
            throw new CustomException(ErrorCode.INVALID_CHARGE_AMOUNT);
        }
        this.pointBalance += amount;
    }
    public void deductPoint(int amount) {
        if (this.pointBalance < amount) {
            throw new CustomException(ErrorCode.INSUFFICIENT_POINT);
        }
        this.pointBalance -= amount;
    }
}
