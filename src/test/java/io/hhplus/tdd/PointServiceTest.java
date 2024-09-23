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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
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
    void selectUserPointTest(){
        //given
        long userId = 1L;
        long point = 10000L;
        UserPoint userPoint = new UserPoint(userId,point,System.currentTimeMillis());

        //when
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        //then
        assertThat(pointService.selectUserPoint(userId).point()).isEqualTo(userPoint.point());
    }

    // 특정 유저의 포인트 충전/이용 내역 조회 -> userPointHistory 메서드
    @Test
    @DisplayName("특정 유저의 포인트 충전/이용 내역 조회")
    void userPointHistoryTest(){
        //given
        long userId = 1L;
        PointHistory user1 = new PointHistory(1L, userId,1000, TransactionType.CHARGE,System.currentTimeMillis());
        PointHistory user2 = new PointHistory(1L, userId,2000, TransactionType.USE,System.currentTimeMillis());

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
    void throwsErrorWhenAmountIsZeroOrLess(){
        //given
        long userId = 1L;

        //when && then
        IllegalArgumentException zeroException = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(userId, 0);
        });

        IllegalArgumentException negativeException = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(userId, -100);
        }) ;

        Assertions.assertEquals("충전 금액은 최소 1원 이상이어야 합니다.",zeroException.getMessage());
        Assertions.assertEquals("충전 금액은 최소 1원 이상이어야 합니다.",negativeException.getMessage());

    }

    @Test
    @DisplayName("정상 금액 충전 시 정상 충전 되는지")
    void chargeAmountTest(){
        //given
        long userId = 1L, amount = 1000;
        long chargeMoney = 1000; // 충전 금액
        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis()); // 충전 전

        //유연하지 않은 테스트 방식
        //when
        //when(userPointTable.insertOrUpdate(userPoint.id(),userPoint.point() + chargeMoney)).thenReturn(chargeUserPoint);

        //UserPoint result = pointService.charge(userId,chargeMoney);

        //then
        //assertThat(result.point()).isEqualTo(chargeUserPoint.point());

        //when
        when(pointService.selectUserPoint(userId)).thenReturn(userPoint);
        //invocation : thenAnswer() 의 인수. 호출된 메서드와 그 인수를 처리할 수 있게 해줌
        when(userPointTable.insertOrUpdate(anyLong(),anyLong())).thenAnswer(invocation -> {
            long updateUserId = invocation.getArgument(0);
            long updateUserAmount = invocation.getArgument(1);
            return new UserPoint(updateUserId,updateUserAmount,System.currentTimeMillis());
        });

        UserPoint updateUserPoint = pointService.charge(userId,chargeMoney);

        //then
        assertThat(updateUserPoint.point()).isEqualTo(amount + chargeMoney);
    }

}
