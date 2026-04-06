package com.example.coffeeshop;

import com.example.coffeeshop.domain.menu.dto.PopularMenuResponse;
import com.example.coffeeshop.domain.menu.entity.Menu;
import com.example.coffeeshop.domain.menu.repository.MenuRepository;
import com.example.coffeeshop.domain.menu.repository.PopularMenuCacheRepository;
import com.example.coffeeshop.domain.menu.service.PopularMenuService;
import com.example.coffeeshop.domain.order.dto.OrderRequest;
import com.example.coffeeshop.domain.order.repository.OrderRepository;
import com.example.coffeeshop.domain.order.service.OrderService;
import com.example.coffeeshop.domain.user.dto.ChargeRequest;
import com.example.coffeeshop.domain.user.entity.User;
import com.example.coffeeshop.domain.user.repository.PointHistoryRepository;
import com.example.coffeeshop.domain.user.repository.UserRepository;
import com.example.coffeeshop.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PopularMenuServiceTest {

    @Autowired private PopularMenuService popularMenuService;
    @Autowired private OrderService orderService;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private MenuRepository menuRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PointHistoryRepository pointHistoryRepository;
    @Autowired private PopularMenuCacheRepository popularMenuCacheRepository;

    private Long userId;
    private Long menuId1;
    private Long menuId2;
    private Long menuId3;

    @BeforeEach
    void setUp() {
        // 테스트 전 DB 초기화
        popularMenuCacheRepository.deleteAll();
        pointHistoryRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
        menuRepository.deleteAll();

        // 유저 생성 후 충전
        User user = User.create("집계테스트유저");
        userId = userRepository.save(user).getId();
        userService.chargePoint(userId, new ChargeRequest(100000));

        // 메뉴 생성
        menuId1 = menuRepository.save(Menu.createForTest("아메리카노", 4500)).getId();
        menuId2 = menuRepository.save(Menu.createForTest("카페라떼", 5000)).getId();
        menuId3 = menuRepository.save(Menu.createForTest("바닐라라떼", 5500)).getId();
    }

    @Test
    @DisplayName("주문 횟수가 많은 순서대로 인기 메뉴 3개가 정확히 반환되어야 한다")
    void 인기메뉴_집계_정확성_테스트() {
        // 아메리카노 5번, 카페라떼 3번, 바닐라라떼 1번 주문
        for (int i = 0; i < 5; i++) orderService.order(new OrderRequest(userId, menuId1));
        for (int i = 0; i < 3; i++) orderService.order(new OrderRequest(userId, menuId2));
        orderService.order(new OrderRequest(userId, menuId3));

        // 집계 실행
        popularMenuService.aggregatePopularMenus();

        // 조회
        PopularMenuResponse response = popularMenuService.getPopularMenus();

        assertThat(response.getPopularMenus()).hasSize(3);
        assertThat(response.getPopularMenus().get(0).getMenuId()).isEqualTo(menuId1);
        assertThat(response.getPopularMenus().get(0).getOrderCount()).isEqualTo(5);
        assertThat(response.getPopularMenus().get(1).getMenuId()).isEqualTo(menuId2);
        assertThat(response.getPopularMenus().get(1).getOrderCount()).isEqualTo(3);
        assertThat(response.getPopularMenus().get(2).getMenuId()).isEqualTo(menuId3);
        assertThat(response.getPopularMenus().get(2).getOrderCount()).isEqualTo(1);
    }
}