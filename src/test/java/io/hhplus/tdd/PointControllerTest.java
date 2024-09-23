package io.hhplus.tdd;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.point.controller.PointController;
import io.hhplus.tdd.point.repository.PointHistory;
import io.hhplus.tdd.point.repository.TransactionType;
import io.hhplus.tdd.point.repository.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PointController.class)
public class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    PointService pointService;

    // point/{id} Test
    @Test
    @DisplayName("userPoint를 잘 반환하는가")
    void show_userPoint_data() throws Exception {

        //given
        UserPoint userPoint = new UserPoint(1L, 1000, System.currentTimeMillis());

        //when
        when(pointService.selectUserPoint(userPoint.id())).thenReturn(userPoint);

        //then
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get("/point/{id}", 1L))
                .andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andExpect(status().isOk())
                .andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString());

    }

    // point/{id}/histories Test
    @Test
    @DisplayName("PointHistory 내역을 반환하는가")
    void show_userPoint_History() throws Exception {
        //given
        long userId = 1L;
        PointHistory user1 = new PointHistory(1L, userId, 1000, TransactionType.CHARGE, System.currentTimeMillis());
        PointHistory user2 = new PointHistory(1L, userId, 2000, TransactionType.USE, System.currentTimeMillis());

        List<PointHistory> pointHistories = new ArrayList<>();
        pointHistories.add(user1);
        pointHistories.add(user2);

        //when
        when(pointService.userPointHistory(userId)).thenReturn(pointHistories);

        //then
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get("/point/{id}/histories", userId))
                .andExpect(jsonPath("$").isArray())
                .andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        System.out.println(mvcResult.getResponse().getContentAsString());
    }

    //point/{id}/charge Test
    @Test
    @DisplayName("포인트 충전")
    void chargeTest() throws Exception {
        //given
        long userId = 1L;
        UserPoint userPoint = new UserPoint(userId, 1000, System.currentTimeMillis());

        //when
        when(pointService.charge(anyLong(), anyLong())).thenReturn(userPoint);

        //then
        mockMvc.perform(
                patch("/point/{id}/charge",userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userPoint.point())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userPoint.id()))
                .andExpect(jsonPath("$.point").value(userPoint.point()));
    }
}
