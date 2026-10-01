package com.sky.controller;

import com.sky.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id){
        return new User(id,"Tom",20);
    }

    @GetMapping
    public List<User> search(@RequestParam String name){
        return List.of(new User(1L,name,18),new User(2L,name,30));
    }

    @PostMapping
    public User create(@RequestBody User user){
        return new User(99L,user.name(),user.age());
    }

}
