package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.repository.PointHistory;
import io.hhplus.tdd.point.repository.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ConcurrencyTest {

    private final PointService pointService;

    private final UserPointTable userPointTable;

    private final PointHistoryTable pointHistoryTable;

    @Autowired
    public ConcurrencyTest(PointService pointService, UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.pointService = pointService;
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    @Test
    @DisplayName("동시에 여러 건의 포인트 충전&사용이 들어올 때 순차적으로 처리 되는가")
    void chargeAndUsePointsSequentially() throws InterruptedException {
        int numThreads = 10;
        //ExecutorService : 스레드 작업 등록&실행을 위한 책임을 가짐.
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        //CountDownLatch : 하나 이상의 스레드가 다른 스레드가 완료될 때까지 기다리게 함.
        CountDownLatch latch = new CountDownLatch(numThreads);

        Runnable runnable = (() -> {
            try {
                pointService.charge(1L, 10);
                pointService.use(1L, 5);
            }finally {
                latch.countDown();
            }
        });

        for (int i = 0; i < numThreads; i++){
                executor.submit(runnable);
        }

        latch.await();
        executor.shutdown();

        UserPoint userPoint = userPointTable.selectById(1L);
        assertThat(userPoint.point()).isEqualTo(50);

        List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(userPoint.id());
        assertThat(pointHistories).hasSize(20);
    }
}
