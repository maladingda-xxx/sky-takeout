package com.sky.takeout.service.impl;

import com.sky.takeout.dto.UserLoginDTO;
import com.sky.takeout.entity.UserAccount;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.UserAccountMapper;
import com.sky.takeout.service.UserService;
import com.sky.takeout.service.WeChatAuthService;
import com.sky.takeout.utils.JwtUtil;
import com.sky.takeout.vo.UserLoginVO;
import com.sky.takeout.vo.UserVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserAccountMapper userAccountMapper;
    private final WeChatAuthService weChatAuthService;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(
            UserAccountMapper userAccountMapper,
            WeChatAuthService weChatAuthService,
            JwtUtil jwtUtil
    ) {
        this.userAccountMapper = userAccountMapper;
        this.weChatAuthService = weChatAuthService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        WeChatAuthService.WeChatSession session =
                weChatAuthService.exchangeCode(userLoginDTO.getCode());

        UserAccount user = userAccountMapper.selectByOpenid(session.openid());
        if (user == null) {
            user = new UserAccount();
            user.setOpenid(session.openid());

            try {
                if (userAccountMapper.insert(user) != 1) {
                    throw new BusinessException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to create user"
                    );
                }
            } catch (DuplicateKeyException exception) {
                user = userAccountMapper.selectByOpenid(session.openid());
            }
        }

        if (user == null || user.getId() == null) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to resolve user account"
            );
        }

        return new UserLoginVO(
                user.getId(),
                user.getOpenid(),
                jwtUtil.generateUserToken(user.getId())
        );
    }

    @Override
    public UserVO getById(Long id) {
        UserAccount user = userAccountMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND.value(),
                    "User not found"
            );
        }

        return new UserVO(
                user.getId(),
                user.getName(),
                user.getPhone(),
                user.getSex(),
                user.getAvatar()
        );
    }
}
