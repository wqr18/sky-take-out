package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @RequestMapping("/save")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}", dishDTO);
        return Result.success();
    }
}
