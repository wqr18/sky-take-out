package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);


    void insert(User user);

    Integer countUser(Map map);

    /**
     * 按日期分组统计新增用户数
     */
    List<Map<String, Object>> countNewUserByDay(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 查询指定时间之前的总用户数（用于累计计算）
     */
    Integer countUserBefore(LocalDateTime beginTime);
}