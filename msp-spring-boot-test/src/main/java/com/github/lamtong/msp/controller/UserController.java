package com.github.lamtong.msp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lamtong.msp.entity.User;
import com.github.lamtong.msp.mapper.UserMapper;
import com.meilisearch.sdk.model.IndexStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RequestMapping(value = "user")
@RestController
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping(value = "stat")
    public String getState() throws JsonProcessingException {
        IndexStats stats = userMapper.getStats();
        return new ObjectMapper().writeValueAsString(stats);
    }

    @GetMapping(value = "insert")
    public User insert() {
        User user = new User();
        user.setId(1L);
        user.setName("Lam Tong");
        user.setPassword("abc123");
        user.setAddress("xxx");
        user.setBirth(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        user.setRemark("remark is empty");
        this.userMapper.insert(user);
        return user;
    }

}
