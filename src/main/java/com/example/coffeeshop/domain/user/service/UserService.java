package com.example.coffeeshop.domain.user.service;

import com.example.coffeeshop.domain.user.dto.ChargeRequest;
import com.example.coffeeshop.domain.user.dto.ChargeResponse;
import com.example.coffeeshop.domain.user.entity.PointHistory;
import com.example.coffeeshop.domain.user.entity.PointType;
import com.example.coffeeshop.domain.user.entity.User;
import com.example.coffeeshop.domain.user.repository.PointHistoryRepository;
import com.example.coffeeshop.domain.user.repository.UserRepository;
import com.example.coffeeshop.global.exception.CustomException;
import com.example.coffeeshop.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    public ChargeResponse chargePoint(Long userId, ChargeRequest request) {
        // 비관적 락으로 유저 조회
        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 포인트 충전
        user.chargePoint(request.getAmount());

        // 충전 이력 저장
        PointHistory history = PointHistory.create(
                user,
                request.getAmount(),
                PointType.CHARGE,
                user.getPointBalance()
        );
        pointHistoryRepository.save(history);

        return new ChargeResponse(userId, request.getAmount(), user.getPointBalance());
    }
}
