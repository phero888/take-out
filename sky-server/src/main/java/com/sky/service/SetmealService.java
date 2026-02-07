package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface SetmealService {

    /**
     * 套餐分页查询
     * @param setMealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setMealPageQueryDTO);

    /**
     * 新增套餐
     * @param setmealDTO
     */
    void save(SetmealDTO setmealDTO);

    /**
     * 批量删除套餐及套餐菜品关系
     * @param ids
     */
    void deleteBatchWithDish(List<Long> ids);

    /**
     * 起售停售套餐
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);
}
