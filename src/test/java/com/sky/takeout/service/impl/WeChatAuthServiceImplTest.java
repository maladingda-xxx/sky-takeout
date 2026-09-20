package com.sky.takeout.service.impl;

import com.sky.takeout.config.WeChatProperties;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.service.WeChatAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WeChatAuthServiceImplTest {

    private WeChatProperties properties;
    private MockRestServiceServer server;
    private WeChatAuthServiceImpl weChatAuthService;

    @BeforeEach
    void setUp() {
        properties = new WeChatProperties();
        properties.setAppId("test-app-id");
        properties.setAppSecret("test-app-secret");
        properties.setApiUrl("http://127.0.0.1:9999/sns/jscode2session");

        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        weChatAuthService = new WeChatAuthServiceImpl(properties, builder);
    }

    @Test
    void shouldExchangeCodeThroughWeChatApi() {
        server.expect(requestTo(
                        "http://127.0.0.1:9999/sns/jscode2session"
                                + "?appid=test-app-id"
                                + "&secret=test-app-secret"
                                + "&js_code=valid-code"
                                + "&grant_type=authorization_code"
                ))
                .andRespond(withSuccess(
                        """
                                {
                                  "openid": "openid-123",
                                  "session_key": "session-key"
                                }
                                """,
                        MediaType.APPLICATION_JSON
                ));

        WeChatAuthService.WeChatSession session =
                weChatAuthService.exchangeCode("valid-code");

        assertEquals("openid-123", session.openid());
        assertEquals("session-key", session.sessionKey());
        server.verify();
    }

    @Test
    void shouldRejectWeChatErrorResponse() {
        server.expect(requestTo(
                        "http://127.0.0.1:9999/sns/jscode2session"
                                + "?appid=test-app-id"
                                + "&secret=test-app-secret"
                                + "&js_code=invalid-code"
                                + "&grant_type=authorization_code"
                ))
                .andRespond(withSuccess(
                        """
                                {
                                  "errcode": 40029,
                                  "errmsg": "invalid code"
                                }
                                """,
                        MediaType.APPLICATION_JSON
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> weChatAuthService.exchangeCode("invalid-code")
        );

        assertEquals(401, exception.getCode());
        assertEquals("WeChat authentication failed", exception.getMessage());
    }

    @Test
    void shouldSupportLocalMockMode() {
        properties.setMockEnabled(true);

        WeChatAuthService.WeChatSession session =
                weChatAuthService.exchangeCode("demo");

        assertEquals("mock-demo", session.openid());
        assertEquals("mock-session", session.sessionKey());
    }
}
