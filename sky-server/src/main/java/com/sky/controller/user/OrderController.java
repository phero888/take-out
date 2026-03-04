package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.service.impl.OrderServiceImpl;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user/order")
@Slf4j
public class OrderController {

    @Autowired
    public OrderService orderService;

    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户下单：{}",ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);

    }

    @PostMapping("/pay")
    public Result<OrderPaymentVO> pay(@RequestBody OrdersPaymentDTO ordersPaymentDTO){
        log.info("用户支付");
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        return Result.success(orderPaymentVO);
    }
    @GetMapping("/historyOrders")
    public Result<PageResult> pageQuery(@RequestBody OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("查询历史订单：{}",ordersPageQueryDTO);
        PageResult pageResult = orderService.pageQuery4user(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> details(@PathVariable Long id){
        log.info("订单详细信息：{}",id);
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }
}
