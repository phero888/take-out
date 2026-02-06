package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;

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
}
