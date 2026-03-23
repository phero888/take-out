package com.sky.service.impl;

import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public BusinessDataVO getBusinessData() {
        LocalDateTime begin = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        Double turnover = orderMapper.sumAmountByDate(begin, end);
        Integer newUsers = userMapper.countByCreateTime(begin, end);
        Integer validOrderCount = orderMapper.countByDate(begin, end);
        Integer orderCount = orderMapper.countByDate(begin, end);

        Double unitPrice = 0.0;
        if (validOrderCount > 0) {
            unitPrice = turnover / validOrderCount;
        }

        Double orderCompletionRate = 0.0;
        if (orderCount > 0) {
            orderCompletionRate = (double) validOrderCount / orderCount;
        }

        return BusinessDataVO.builder()
                .turnover(turnover)
                .newUsers(newUsers)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .build();
    }

    @Override
    public SetmealOverViewVO getSetmealOverview() {
        Integer sold = setmealMapper.countByStatus(1);
        Integer discontinued = setmealMapper.countByStatus(0);
        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    @Override
    public DishOverViewVO getDishOverview() {
        Integer sold = dishMapper.countByStatus(1);
        Integer discontinued = dishMapper.countByStatus(0);
        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    @Override
    public OrderOverViewVO getOrderOverview() {
        Integer allOrders = orderMapper.countAll();
        Integer waitingOrders = orderMapper.countByStatus(2);
        Integer deliveredOrders = orderMapper.countByStatus(3);
        Integer completedOrders = orderMapper.countByStatus(4);
        Integer cancelledOrders = orderMapper.countByStatus(5);
        return OrderOverViewVO.builder()
                .allOrders(allOrders)
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .build();
    }
}
