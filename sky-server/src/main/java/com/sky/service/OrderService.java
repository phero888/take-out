package com.sky.service;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO);

    /**
     * 用户分页查询历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult pageQuery4user(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 查询订单详细信息
     * @param id
     * @return
     */
    OrderVO details(Long id);
}
