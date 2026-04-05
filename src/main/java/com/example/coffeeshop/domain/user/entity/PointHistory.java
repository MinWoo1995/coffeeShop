package com.example.coffeeshop.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointType type;

    @Column(nullable = false)
    private int balanceAfter;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static PointHistory create(User user, int amount, PointType type, int balanceAfter) {
        PointHistory history = new PointHistory();
        history.user = user;
        history.amount = amount;
        history.type = type;
        history.balanceAfter = balanceAfter;
        return history;
    }
}
