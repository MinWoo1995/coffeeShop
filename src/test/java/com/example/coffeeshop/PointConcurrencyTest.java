package com.example.coffeeshop;

import com.example.coffeeshop.domain.user.dto.ChargeRequest;
import com.example.coffeeshop.domain.user.entity.User;
import com.example.coffeeshop.domain.user.repository.UserRepository;
import com.example.coffeeshop.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PointConcurrencyTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        User user = User.create("동시성테스트유저");
        userId = userRepository.save(user).getId();
    }

    @Test
    @DisplayName("100개 스레드가 동시에 1000P씩 충전하면 최종 잔액은 100000P여야 한다")
    void 동시_포인트_충전_테스트() throws InterruptedException {
        int threadCount = 100;
        int chargeAmount = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    ChargeRequest request = new ChargeRequest(chargeAmount);
                    userService.chargePoint(userId, request);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        User user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getPointBalance()).isEqualTo(threadCount * chargeAmount);
    }
}