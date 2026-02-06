package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/setmeal")
public class SetmealController {
    @Autowired
    SetmealService setMealService;
    /**
     * 套餐分页查询
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setMealPageQueryDTO ){
        log.info("套餐分页查询...");
        PageResult pageResult = setMealService.pageQuery(setMealPageQueryDTO);
        return Result.success(pageResult);
    }

//    新增套餐
    @PostMapping
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐...");
        setMealService.save(setmealDTO);
        return Result.success();
    }
//    删除套餐
//    修改套餐
//    起售停售套餐
}
