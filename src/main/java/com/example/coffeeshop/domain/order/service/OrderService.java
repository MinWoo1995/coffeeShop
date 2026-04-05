package com.example.coffeeshop.domain.order.service;

import com.example.coffeeshop.domain.menu.entity.Menu;
import com.example.coffeeshop.domain.menu.repository.MenuRepository;
import com.example.coffeeshop.domain.order.dto.OrderRequest;
import com.example.coffeeshop.domain.order.dto.OrderResponse;
import com.example.coffeeshop.domain.order.entity.Order;
import com.example.coffeeshop.domain.order.repository.OrderRepository;
import com.example.coffeeshop.domain.user.entity.PointHistory;
import com.example.coffeeshop.domain.user.entity.PointType;
import com.example.coffeeshop.domain.user.entity.User;
import com.example.coffeeshop.domain.user.repository.PointHistoryRepository;
import com.example.coffeeshop.domain.user.repository.UserRepository;
import com.example.coffeeshop.global.platform.DataPlatformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final DataPlatformService dataPlatformService;

    @Transactional
    public OrderResponse order(OrderRequest request) {

        //비관적 락으로 유저 조회
        User user = userRepository.findByIdWithLock(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        //메뉴 조회 (판매 중단 체크)
        Menu menu = menuRepository.findByIdAndIsAvailableTrue(request.getMenuId())
                .orElseThrow(() -> new IllegalArgumentException("주문할 수 없는 메뉴입니다."));

        //포인트 차감 (잔액 부족 시 예외 발생)
        user.deductPoint(menu.getPrice());

        //주문 생성
        Order order = Order.create(user, menu);
        orderRepository.save(order);

        //포인트 차감 이력 저장
        PointHistory history = PointHistory.create(
                user,
                menu.getPrice(),
                PointType.ORDER_PAYMENT,
                user.getPointBalance()
        );
        pointHistoryRepository.save(history);

        //데이터 플랫폼 비동기 전송 (트랜잭션 밖에서 동작)
        dataPlatformService.send(user.getId(), menu.getId(), menu.getPrice());

        return new OrderResponse(
                order.getId(),
                user.getId(),
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                user.getPointBalance(),
                order.getOrderedAt()
        );
    }
}