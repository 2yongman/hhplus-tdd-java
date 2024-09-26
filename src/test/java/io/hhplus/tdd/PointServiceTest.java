package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.repository.PointHistory;
import io.hhplus.tdd.point.repository.TransactionType;
import io.hhplus.tdd.point.repository.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointService pointService;

    //포인트 조회 테스트 -> selectUserPoint 메서드
    @Test
    @DisplayName("포인트 조회 테스트")
    void selectUserPointTest() {
        //given
        long userId = 1L;
        long point = 10000L;
        UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());
git p
        //when
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        //then
        assertThat(pointService.selectUserPoint(userId).point()).isEqualTo(userPoint.point());
    }

    // 특정 유저의 포인트 충전/이용 내역 조회 -> userPointHistory 메서드
    @Test
    @DisplayName("특정 유저의 포인트 충전/이용 내역 조회")
    void userPointHistoryTest() {
        //given
        long userId = 1L;
        PointHistory user1 = new PointHistory(1L, userId, 1000, TransactionType.CHARGE, System.currentTimeMillis());
        PointHistory user2 = new PointHistory(1L, userId, 2000, TransactionType.USE, System.currentTimeMillis());

        List<PointHistory> pointHistories = new ArrayList<>();
        pointHistories.add(user1);
        pointHistories.add(user2);

        //when
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(pointHistories);

        //then
        assertThat(pointService.userPointHistory(userId).size()).isEqualTo(pointHistories.size());
        assertThat(pointService.userPointHistory(userId)).isEqualTo(pointHistories);
    }

    // 충전 - charge 메서드
    @Test
    @DisplayName("충전 금액이 0원일 때 에러를 발생시키는가")
    void throwsErrorWhenAmountIsZeroOrLess() {
        //given
        long userId = 1L;

        //when && then
        IllegalArgumentException zeroException = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(userId, 0);
        });

        IllegalArgumentException negativeException = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(userId, -100);
        });

        Assertions.assertEquals("충전 금액은 최소 1원 이상이어야 합니다.", zeroException.getMessage());
        Assertions.assertEquals("충전 금액은 최소 1원 이상이어야 합니다.", negativeException.getMessage());

    }

    @Test
    @DisplayName("정상 금액 충전 시 정상 충전 되는지")
    void chargeAmountTest() {
        //given
        long userId = 1L, amount = 1000;
        long chargeMoney = 1000; // 충전 금액
        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis()); // 충전 전

        //유연하지 않은 테스트 방식
        //when
        //(userPointTable.insertOrUpdate(userPoint.id(),userPoint.point() + chargeMoney)).thenReturn(chargeUserPoint);

        //UserPoint result = pointService.charge(userId,chargeMoney);

        //then
        //assertThat(result.point()).isEqualTo(chargeUserPoint.point());

        //when
        when(pointService.selectUserPoint(userId)).thenReturn(userPoint);
        //invocation : thenAnswer() 의 인수. 호출된 메서드와 그 인수를 처리할 수 있게 해줌
        when(userPointTable.insertOrUpdate(anyLong(), anyLong())).thenAnswer(invocation -> {
            long updateUserId = invocation.getArgument(0);
            long updateUserAmount = invocation.getArgument(1);
            return new UserPoint(updateUserId, updateUserAmount, System.currentTimeMillis());
        });

        UserPoint updateUserPoint = pointService.charge(userId, chargeMoney);

        //then
        assertThat(updateUserPoint.point()).isEqualTo(amount + chargeMoney);
    }


    @Test
    @DisplayName("정상적인 사용_사용포인트가 잔여포인트보다 작을 때")
        // 잔여포인트 > 사용포인트
    void usePoints_SuccessWhenEnoughPoints() {
        //given
        long userId = 1L, initAmount = 10000;
        long useAmount = 5000;
        UserPoint initUserPoint = new UserPoint(userId, initAmount, System.currentTimeMillis());

        //when
        when(userPointTable.selectById(userId)).thenReturn(initUserPoint);

        when(userPointTable.insertOrUpdate(anyLong(), anyLong())).thenAnswer(invocation -> {
            long updateUserId = invocation.getArgument(0);
            long updateAmount = invocation.getArgument(1);
            return new UserPoint(updateUserId, updateAmount, System.currentTimeMillis());
        });

        UserPoint updateUserPoint = pointService.use(userId, useAmount);

        //then
        assertThat(updateUserPoint.point()).isEqualTo(initAmount - useAmount);
    }

    @Test
    @DisplayName("비정상적인 사용_사용포인트가 잔여포인트보다 많을 때")
    void usePoints_FailWhenLessPoints() {
        //given
        long userId = 1L, amount = 1000;
        long useAmount = 2000;
        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        //when
        when(userPointTable.selectById(userId)).thenReturn(userPoint);
        IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(userId, useAmount);
        });

        //then
        assertThat(illegalArgumentException.getMessage()).isEqualTo("잔여 포인트가 부족합니다.");
        assertThat(userPointTable.selectById(userId).point()).isEqualTo(amount);
    }

    @Test
    @DisplayName("유효하지 않은 ID 예외")
    void fail_WhenIdIsInvalid() {
        long id = -1L, amount = 1000;

        IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(id, amount);
        });

        //then
        assertThat(illegalArgumentException.getMessage()).isEqualTo("유효하지 않은 ID 값입니다.");
    }

    @Test
    @DisplayName("포인트 충전&사용 시 포인트 내역에 추가되는지")
    void addPointHistory_OnPointChargeAndUse() {
        //given
        long userId = 1L, amount = 1000;
        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());


        //when
        when(userPointTable.selectById(userId)).thenReturn(userPoint);
        when(pointHistoryTable.insert(eq(userId), anyLong(), any(), anyLong()))
                .thenAnswer(invocation -> {
                    long invoId = invocation.getArgument(0);
                    long invoAmount = invocation.getArgument(1);
                    TransactionType type = invocation.getArgument(2);
                    long time = invocation.getArgument(3);

                    return new PointHistory(invoId, userId, invoAmount, type, time);
                });

        //then
        pointService.charge(userId, 1000);
        verify(pointHistoryTable, times(1)).insert(eq(userId), anyLong(), eq(TransactionType.CHARGE), anyLong());
        pointService.charge(userId, 2000);
        verify(pointHistoryTable, times(2)).insert(eq(userId), anyLong(), eq(TransactionType.CHARGE), anyLong());

        pointService.use(userId, 1000);
        verify(pointHistoryTable, times(1)).insert(eq(userId), anyLong(), eq(TransactionType.USE), anyLong());
    }

    @Test
    @DisplayName("최대 잔고를 넘어 충전을 했는지")
    void exceededMaxAmount() {
        //given
        long userId = 1L, amount = 100000;
        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        //when
        long chargeAmount = 900005;
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        IllegalStateException illegalStateException = Assertions.assertThrows(IllegalStateException.class, () -> {
            pointService.charge(userId,chargeAmount);
        });

        assertThat(illegalStateException.getMessage()).isEqualTo("최대 잔고를 초과할 수 없습니다.");
    }
}
