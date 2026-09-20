package com.sky.takeout.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SystemController {

    @GetMapping("/hello")
    public String hello() {
        return "Sky Takeout is running";
    }
}
