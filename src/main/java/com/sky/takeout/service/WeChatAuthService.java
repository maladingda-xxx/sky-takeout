package com.sky.takeout.service;

public interface WeChatAuthService {

    WeChatSession exchangeCode(String code);

    record WeChatSession(String openid, String sessionKey) {
    }
}
