package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 批量添加套餐菜品关系
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 通过setmeal_id批量删除套餐对应菜品信息
     * @param ids
     */
    void deleteBySetmealIdBatch(List<Long> ids);

    /**
     * 通过套餐id查询菜品id
     * @param id
     * @return
     */
    @Select("select dish_id from setmeal_dish where setmeal_id = #{setmealId}")
    List<Long> getDishIdBySetmealId(Long setmealId);
}
