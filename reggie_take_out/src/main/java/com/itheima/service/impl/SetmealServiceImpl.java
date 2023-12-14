package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomException;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Setmeal;
import com.itheima.entity.SetmealDish;
import com.itheima.mapper.SetmealMapper;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     *
     * @param setmealDto
     */

    @Autowired
    private SetmealDishService setmealDishService;


    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {

        // 保存套餐的基本信息,操作setmeal，执行insert操作
        this.save(setmealDto);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setDishId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());


        // 保存套餐和菜品的关系，操作setmeal_dish,执行insert操作
        setmealDishService.saveBatch(setmealDishes);

    }


    /**
     * 删除套餐，同时需要删除套餐和菜品的关联关系
     */

    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        // 查询套餐状态，确定是否可以删除，只有状态是停售的 才能删除   (1为在售)
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);

        // 将查询结果封装在count中
        int count = this.count(queryWrapper);

        if (count > 0) {
            // 如果不能删除，则抛出一个业务异常（当前套餐正在售卖中，无法删除）
            throw new CustomException("套餐正在售卖中，无法删除!");
        }
        // 如果可以删除,先删除套餐表中的数据 --setmeal
        this.removeByIds(ids);
        // 删除关系表中的数据 --setmeal_dish
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        // 添加条件
        queryWrapper1.in(SetmealDish::getSetmealId, ids);
        // 执行删除语句
        setmealDishService.remove(queryWrapper1);

    }
}
