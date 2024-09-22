package io.hhplus.tdd;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.repository.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    private UserPointTable userPointTable;

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

}
