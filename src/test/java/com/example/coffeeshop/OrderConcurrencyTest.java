package com.example.coffeeshop;

import com.example.coffeeshop.domain.menu.entity.Menu;
import com.example.coffeeshop.domain.menu.repository.MenuRepository;
import com.example.coffeeshop.domain.order.dto.OrderRequest;
import com.example.coffeeshop.domain.order.repository.OrderRepository;
import com.example.coffeeshop.domain.order.service.OrderService;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderConcurrencyTest {

    @Autowired private OrderService orderService;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private MenuRepository menuRepository;
    @Autowired private OrderRepository orderRepository;

    private Long userId;
    private Long menuId;

    @BeforeEach
    void setUp() {
        // 유저 생성 후 10000P 충전
        User user = User.create("주문동시성테스트");
        userId = userRepository.save(user).getId();
        userService.chargePoint(userId, new ChargeRequest(10000));

        // 4500P 메뉴 생성
        Menu menu = Menu.createForTest("테스트아메리카노", 4500);
        menuId = menuRepository.save(menu).getId();
    }

    @Test
    @DisplayName("잔액 10000P에 4500P짜리 메뉴를 동시에 10번 주문하면 2번만 성공해야 한다")
    void 동시_주문_테스트() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    orderService.order(new OrderRequest(userId, menuId));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 10000 / 4500 = 2번만 성공 가능
        assertThat(successCount.get()).isEqualTo(2);
        assertThat(failCount.get()).isEqualTo(8);

        User user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getPointBalance()).isEqualTo(10000 - (4500 * 2));
    }
}