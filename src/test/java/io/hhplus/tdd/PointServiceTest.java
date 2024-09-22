package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.repository.PointHistory;
import io.hhplus.tdd.point.repository.TransactionType;
import io.hhplus.tdd.point.repository.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

}
