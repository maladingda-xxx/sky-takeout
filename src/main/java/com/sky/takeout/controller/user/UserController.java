package com.sky.takeout.controller.user;

import com.sky.takeout.common.Result;
import com.sky.takeout.common.UserContext;
import com.sky.takeout.dto.UserLoginDTO;
import com.sky.takeout.service.UserService;
import com.sky.takeout.vo.UserLoginVO;
import com.sky.takeout.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        return Result.success(userService.login(userLoginDTO));
    }

    @GetMapping("/profile")
    public Result<UserVO> profile() {
        return Result.success(userService.getById(UserContext.getRequiredUserId()));
    }
}
