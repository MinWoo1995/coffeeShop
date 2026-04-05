package com.example.coffeeshop.global.platform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DataPlatformService {

    @Async
    public void send(Long userId, Long menuId, int price) {
        try {
            // 실제 환경에서는 외부 API 호출
            // 여기서는 Mock으로 로그만 출력
            Thread.sleep(100); // 외부 API 호출 시뮬레이션
            log.info("[DataPlatform] 전송 성공 - userId: {}, menuId: {}, price: {}", userId, menuId, price);
        } catch (Exception e) {
            // 전송 실패해도 주문은 유효 — 로그만 기록
            log.error("[DataPlatform] 전송 실패 - userId: {}, menuId: {}, price: {}", userId, menuId, price);
        }
    }
}