package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐...");
        setMealService.save(setmealDTO);
        return Result.success();
    }

    /**
     * 批量删除套餐
      * @param ids
     * @return
     */
    @DeleteMapping
    public Result deleteBatch(@RequestParam List<Long>ids){
        log.info("批量删除套餐");
        setMealService.deleteBatchWithDish(ids);
        return Result.success();
    }
//    修改套餐

    /**
     * 起售停售套餐
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, @RequestParam Long id){
        log.info("修改套餐状态：{}", status);
        setMealService.startOrStop(status,id);
        return Result.success();
    }
}
