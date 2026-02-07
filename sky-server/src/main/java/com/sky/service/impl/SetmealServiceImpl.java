package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    SetmealMapper setmealMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;
    @Autowired
    DishMapper dishMapper;
    /**
     * 套餐分页查询
     * @param setMealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setMealPageQueryDTO) {
        PageHelper.startPage(setMealPageQueryDTO.getPage(),setMealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setMealPageQueryDTO);
        PageResult pageResult = new PageResult(page.getTotal(), page.getResult());
        return pageResult;
    }

    @Override
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        //添加套餐信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);
        //添加套餐菜品关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for(SetmealDish sd: setmealDishes) sd.setSetmealId(setmeal.getId());
        setmealDishMapper.insertBatch(setmealDishes);

    }

    /**
     * 批量删除套餐及套餐菜品关系
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatchWithDish(List<Long> ids) {
        //查询套餐状态
        Integer sum = setmealMapper.getStatusByIdBatch(ids);
        if(sum>0) throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        //删除套餐
        setmealMapper.deleteBatch(ids);
        //删除套餐相应菜品信息
        setmealDishMapper.deleteBySetmealIdBatch(ids);
    }

    @Override
    @Transactional
    public void startOrStop(Integer status, Long id) {
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        //停售
        if(status ==0) setmealMapper.update(setmeal);
        //起售：检查套餐内的所有菜品处于起售状态
        else{
            List<Long> dishId = setmealDishMapper.getDishIdBySetmealId(id);
            Integer DishStatus = dishMapper.getStatusByIdBatch(dishId);
            if(DishStatus == dishId.size()) setmealMapper.update(setmeal);
            else throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
        }
    }
}
