package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/workspace")
@Slf4j
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    @GetMapping("/businessData")
    public Result<BusinessDataVO> businessData(){
        log.info("查询今日运营数据");
        BusinessDataVO businessDataVO = workspaceService.getBusinessData();
        return Result.success(businessDataVO);
    }

    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> overviewSetmeals(){
        log.info("查询套餐总览");
        SetmealOverViewVO setmealOverViewVO = workspaceService.getSetmealOverview();
        return Result.success(setmealOverViewVO);
    }

    @GetMapping("/overviewDishes")
    public Result<DishOverViewVO> overviewDishes(){
        log.info("查询菜品总览");
        DishOverViewVO dishOverViewVO = workspaceService.getDishOverview();
        return Result.success(dishOverViewVO);
    }

    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> overviewOrders(){
        log.info("查询订单管理数据");
        OrderOverViewVO orderOverViewVO = workspaceService.getOrderOverview();
        return Result.success(orderOverViewVO);
    }
}
