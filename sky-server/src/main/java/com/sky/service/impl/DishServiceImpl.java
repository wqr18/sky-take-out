package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.Result;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Override
    @Transactional
    public Result saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        // 保存菜品
        dishMapper.insert(dish);
        // 菜品id
        Long dishId = dish.getId();
        // 保存菜品口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        // 菜品口味id
        flavors.forEach(dishFlavor->dishFlavor.setDishId(dishId));
        if(flavors!=null&&flavors.size()>0){
            dishFlavorMapper.insertBatch(flavors);
        }

        return Result.success();
    }
}
