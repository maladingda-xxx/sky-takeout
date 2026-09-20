package com.sky.takeout.mapper;

import com.sky.takeout.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAccountMapper {

    UserAccount selectByOpenid(String openid);

    UserAccount selectById(Long id);

    int insert(UserAccount user);
}
