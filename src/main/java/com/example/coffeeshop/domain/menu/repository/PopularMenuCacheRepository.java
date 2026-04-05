package com.example.coffeeshop.domain.menu.repository;

import com.example.coffeeshop.domain.menu.entity.PopularMenuCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PopularMenuCacheRepository extends JpaRepository<PopularMenuCache, Long> {

    @Query("SELECT p FROM PopularMenuCache p JOIN FETCH p.menu " +
            "ORDER BY p.orderCount DESC")
    List<PopularMenuCache> findTopMenus();
}
