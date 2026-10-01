package com.sky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用入口。
 * 放在 com.sky 这个最外层包，@ComponentScan 会从这里向下扫描所有组件。
 */
@SpringBootApplication
public class SkyTakeoutApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkyTakeoutApplication.class, args);
    }
}
