package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/admin/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;
    public static final String DISH = "dish_";
    private static final String PATTERN = DISH +'*';

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        dishService.saveWithFlavor(dishDTO);
        String pattern =  DISH + dishDTO.getCategoryId();
        cleanCache(pattern);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询");
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品删除");
//        //在执行删除之前，获取菜品所在分类（redis精确清空缓存）
//        List<Long> categoryIds = dishService.getCategoryIdByIdBatch(ids);
        dishService.deleteBatch(ids);
//        for (Long categoryId: categoryIds) {
//            String key = DISH + categoryId;
//            redisTemplate.delete(key);
//        }
        cleanCache(PATTERN);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("通过id查询菜品信息：{}",id);
        DishVO dishVO = dishService.getByIdWithFlavors(id);
        return Result.success(dishVO);
    }
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);
        cleanCache(PATTERN);
        return Result.success();
    }
    @PostMapping("/status/{status}")
    public Result updateStatus(@PathVariable Integer status,@RequestParam Long id){
        log.info("修改菜品状态：{}{}",id,status);
        dishService.startOrStop(id,status);
        cleanCache(PATTERN);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<DishVO>> list(@RequestParam Long categoryId){
        log.info("通过分类id查询菜品:{}",categoryId);
        List<DishVO> list = dishService.getByCategoryId(categoryId);
        return Result.success(list);
    }
    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
