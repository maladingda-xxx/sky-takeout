package com.sky.takeout.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sky.takeout.config.WeChatProperties;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.service.WeChatAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WeChatAuthServiceImpl implements WeChatAuthService {

    private final WeChatProperties properties;
    private final RestClient restClient;

    public WeChatAuthServiceImpl(
            WeChatProperties properties,
            RestClient.Builder restClientBuilder
    ) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public WeChatSession exchangeCode(String code) {
        if (properties.isMockEnabled()) {
            return new WeChatSession("mock-" + code.trim(), "mock-session");
        }

        if (isBlank(properties.getAppId()) || isBlank(properties.getAppSecret())) {
            throw new BusinessException(
                    HttpStatus.SERVICE_UNAVAILABLE.value(),
                    "WeChat login is not configured"
            );
        }

        try {
            WeChatSessionResponse response = restClient.get()
                    .uri(UriComponentsBuilder
                            .fromUriString(properties.getApiUrl())
                            .queryParam("appid", properties.getAppId())
                            .queryParam("secret", properties.getAppSecret())
                            .queryParam("js_code", code)
                            .queryParam("grant_type", "authorization_code")
                            .build()
                            .toUri())
                    .retrieve()
                    .body(WeChatSessionResponse.class);

            if (response == null
                    || (response.getErrcode() != null && response.getErrcode() != 0)
                    || isBlank(response.getOpenid())) {
                throw new BusinessException(
                        HttpStatus.UNAUTHORIZED.value(),
                        "WeChat authentication failed"
                );
            }

            return new WeChatSession(response.getOpenid(), response.getSessionKey());
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BusinessException(
                    HttpStatus.BAD_GATEWAY.value(),
                    "WeChat service unavailable"
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static class WeChatSessionResponse {

        private String openid;

        @JsonProperty("session_key")
        private String sessionKey;

        private Integer errcode;
        private String errmsg;

        public String getOpenid() {
            return openid;
        }

        public void setOpenid(String openid) {
            this.openid = openid;
        }

        public String getSessionKey() {
            return sessionKey;
        }

        public void setSessionKey(String sessionKey) {
            this.sessionKey = sessionKey;
        }

        public Integer getErrcode() {
            return errcode;
        }

        public void setErrcode(Integer errcode) {
            this.errcode = errcode;
        }

        public String getErrmsg() {
            return errmsg;
        }

        public void setErrmsg(String errmsg) {
            this.errmsg = errmsg;
        }
    }
}
