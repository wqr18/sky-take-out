package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface DishFlavorMapper {
    /**
     * 批量插入菜品口味
     * @param flavors 菜品口味列表
     */
    void insertBatch(List<DishFlavor> flavors);
}
