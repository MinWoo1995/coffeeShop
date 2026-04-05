package com.example.coffeeshop.domain.user.repository;

import com.example.coffeeshop.domain.user.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
}
