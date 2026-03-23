package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {
    /**
     * 通过openid查询用户
     * @param openid
     */
    @Select("select * from user where openid =#{openid}")
    User getByOpenid(String openid);

    /**
     * 注册新用户
     * @param user
     */
    void insert(User user);

    @Select("select count(*) from user where create_time >= #{begin} and create_time <= #{end}")
    Integer countByCreateTime(LocalDateTime begin, LocalDateTime end);
}
