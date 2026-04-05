package com.example.coffeeshop.domain.menu.service;

import com.example.coffeeshop.domain.menu.dto.PopularMenuResponse;
import com.example.coffeeshop.domain.menu.entity.Menu;
import com.example.coffeeshop.domain.menu.entity.PopularMenuCache;
import com.example.coffeeshop.domain.menu.repository.MenuRepository;
import com.example.coffeeshop.domain.menu.repository.PopularMenuCacheRepository;
import com.example.coffeeshop.domain.order.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularMenuService {

    private static final String REDIS_KEY = "popular:menus";
    private static final int TOP_COUNT = 3;

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final PopularMenuCacheRepository popularMenuCacheRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Scheduled(initialDelay = 0, fixedDelay = 600000)
    @Transactional
    public void aggregatePopularMenus() {
        log.info("[PopularMenu] 인기 메뉴 집계 시작");

        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<Object[]> results = orderRepository.findTopMenusSince(since);

        popularMenuCacheRepository.deleteAll();

        List<PopularMenuCache> caches = new ArrayList<>();
        int limit = Math.min(results.size(), TOP_COUNT);

        for (int i = 0; i < limit; i++) {
            Long menuId = (Long) results.get(i)[0];
            int count = ((Long) results.get(i)[1]).intValue();
            Menu menu = menuRepository.findById(menuId).orElseThrow();
            caches.add(PopularMenuCache.create(menu, count));
        }
        popularMenuCacheRepository.saveAll(caches);

        // Redis에 JSON 문자열로 저장
        try {
            PopularMenuResponse response = buildResponse(caches);
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(REDIS_KEY, json, 10, TimeUnit.MINUTES);
            log.info("[PopularMenu] 인기 메뉴 집계 완료 - {}개", caches.size());
        } catch (JsonProcessingException e) {
            log.error("[PopularMenu] Redis 저장 실패", e);
        }
    }

    @Transactional(readOnly = true)
    public PopularMenuResponse getPopularMenus() {

        // Redis 캐시 조회
        Object cached = redisTemplate.opsForValue().get(REDIS_KEY);
        if (cached != null) {
            try {
                log.info("[PopularMenu] Redis 캐시 히트");
                return objectMapper.readValue((String) cached, PopularMenuResponse.class);
            } catch (JsonProcessingException e) {
                log.error("[PopularMenu] Redis 역직렬화 실패", e);
            }
        }

        // Redis miss → DB 조회
        log.info("[PopularMenu] Redis 캐시 미스 → DB 조회");
        List<PopularMenuCache> caches = popularMenuCacheRepository.findTopMenus();

        if (caches.isEmpty()) {
            return new PopularMenuResponse(List.of(), LocalDateTime.now());
        }

        return buildResponse(caches);
    }

    private PopularMenuResponse buildResponse(List<PopularMenuCache> caches) {
        List<PopularMenuResponse.PopularMenuItem> items = new ArrayList<>();
        for (int i = 0; i < caches.size(); i++) {
            PopularMenuCache cache = caches.get(i);
            items.add(new PopularMenuResponse.PopularMenuItem(
                    i + 1,
                    cache.getMenu().getId(),
                    cache.getMenu().getName(),
                    cache.getMenu().getPrice(),
                    cache.getOrderCount()
            ));
        }
        return new PopularMenuResponse(items, LocalDateTime.now());
    }
}