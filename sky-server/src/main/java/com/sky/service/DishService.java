package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;


public interface DishService {
    /**
     * 新增菜品及口味
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 通过id查询菜品及口味
     *
     * @param id
     * @return
     */
    DishVO getByIdWithFlavors(Long id);

    /**
     *修改菜品及口味
     * @param dishDTO
     */
    void updateWithFlavor(DishDTO dishDTO);

    void startOrStop(Long id, Integer status);

    /**
     * 通过分类id查询菜品
     * @param categoryId
     * @return
     */
    List<DishVO> getByCategoryId(Long categoryId);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);

    /**
     * 通过菜品id批量查询分类id
     * @param ids
     * @return
     */
    List<Long> getCategoryIdByIdBatch(List<Long> ids);
}
