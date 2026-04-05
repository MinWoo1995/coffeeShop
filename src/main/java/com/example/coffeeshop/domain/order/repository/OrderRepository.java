package com.example.coffeeshop.domain.order.repository;

import com.example.coffeeshop.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o.menu.id, COUNT(o) FROM Order o " +
            "WHERE o.orderedAt >= :since " +
            "GROUP BY o.menu.id " +
            "ORDER BY COUNT(o) DESC")
    List<Object[]> findTopMenusSince(@Param("since") LocalDateTime since);
}