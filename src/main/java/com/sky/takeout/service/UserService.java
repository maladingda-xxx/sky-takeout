package com.sky.takeout.service;

import com.sky.takeout.dto.UserLoginDTO;
import com.sky.takeout.vo.UserLoginVO;
import com.sky.takeout.vo.UserVO;

public interface UserService {

    UserLoginVO login(UserLoginDTO userLoginDTO);

    UserVO getById(Long id);
}
