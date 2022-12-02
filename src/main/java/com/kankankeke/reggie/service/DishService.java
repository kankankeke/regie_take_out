package com.kankankeke.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kankankeke.reggie.dto.DishDto;
import com.kankankeke.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    //根据id查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish,dish_flavor
    public void saveDishWithFlavor(DishDto dishDto);

    //更新菜品信息，同时更新对应的口味信息
    public void updateDishWithFlavor(DishDto dishDto);


    //删除套餐，同时需要删除菜品和菜品口味的关联数据
    public void removeWithDish(List<Long> ids);


}
