package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Category;
import com.itheima.entity.Setmeal;
import com.itheima.service.CategoryService;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetMealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     *
     * @param setmealDto
     * @return
     */

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息: {}", setmealDto);

        setmealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功!");
    }

    /**
     * 套餐分页查询
     *
     * @param page
     * @param name
     * @return
     */

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {

        //构造分页构造器
        Page<Setmeal> pageinfo = new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        // 创建条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件，根据name进行模糊查询
        queryWrapper.like(name != null, Setmeal::getName, name);

        // 添加排序条件,根据更新事件降序排列
        queryWrapper.orderByAsc(Setmeal::getUpdateTime);

        // 执行查询
        setmealService.page(pageinfo, queryWrapper);

        // 进行对象拷贝
        BeanUtils.copyProperties(pageinfo, dtoPage, "records");

        List<Setmeal> records = pageinfo.getRecords();

        List<SetmealDto> list =   records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //进行其他信息拷贝
            BeanUtils.copyProperties(item,setmealDto);

            // 分类id
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                // 分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;

        }).collect(Collectors.toList());

        // 注入集合信息
        dtoPage.setRecords(list);
        return R.success(dtoPage);

    }

    /**
     * 删除套餐
     */

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
    log.info("ids:{}",ids);

    setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功!");
    }


    /**
     * 根据categoryId获取套餐信息
     * @param setmeal
     * @return
     */

    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 有两个判断条件分别为categoryId和status  从前端页面放回路由消息可知
        queryWrapper.eq(setmeal.getCategoryId() !=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() !=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);
        return  R.success(list);
    }



}
