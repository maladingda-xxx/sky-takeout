package com.sky.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sky.service.*;
/**
 * 第一个控制器，用来验证 Web 层是否正常。
 * 它在 com.sky 的子包下，所以会被启动类扫描到。
 */
@RestController
public class HelloController {
    private final HelloService helloService;

    public HelloController(HelloService helloService){
        this.helloService=helloService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Spring Boot!";
    }
}
