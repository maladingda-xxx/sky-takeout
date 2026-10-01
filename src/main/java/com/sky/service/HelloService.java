package com.sky.service;

import org.springframework.stereotype.Service;

@Service
public class HelloService {
    public String greet(){
        return "Hello,Spring Boot!";
    }
}
