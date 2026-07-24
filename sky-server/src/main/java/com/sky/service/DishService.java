package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.result.Result;
import org.springframework.web.bind.annotation.RequestBody;

public interface DishService {
    //增菜品 口味
    public Result saveWithFlavor(DishDTO dishDTO);
}
