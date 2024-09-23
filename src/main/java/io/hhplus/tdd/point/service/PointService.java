package io.hhplus.tdd.point.service;

import io.hhplus.tdd.ErrorResponse;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.repository.PointHistory;
import io.hhplus.tdd.point.repository.TransactionType;
import io.hhplus.tdd.point.repository.UserPoint;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {

    private final UserPointTable userPointTable;

    private final PointHistoryTable pointHistoryTable;

    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    // 포인트 조회
    public UserPoint selectUserPoint(long userId){
        return userPointTable.selectById(userId);
    }

    //특정 유저의 포인트 충전/이용 내역을 조회
    public List<PointHistory> userPointHistory(long userId){
        return pointHistoryTable.selectAllByUserId(userId);
    }

    // 포인트 충전
    public UserPoint charge(long userId, long amount){
        //검증사항 - amount가 0이거나 음수이면 안된다.
        if (amount <= 0) throw new IllegalArgumentException("충전 금액은 최소 1원 이상이어야 합니다.");

        // 해당 유저를 조회하고
        UserPoint userPoint = userPointTable.selectById(userId);

        // 유저가 존재하면 유저의 포인트에 amount를 더하고
        long updateAmount = userPoint.point() + amount;

        // 검증사항 - 새로운 userPoint 를 UserPointTable에 insert 해준다.
        UserPoint updateUser = userPointTable.insertOrUpdate(userPoint.id(),updateAmount);

        // 검증사항 - PointHistory 에도 해당 충전 내역을 추가한다.
        pointHistoryTable.insert(userPoint.id(),updateAmount, TransactionType.CHARGE,System.currentTimeMillis());

        return updateUser;
    }

    // 포인트 사용

}
