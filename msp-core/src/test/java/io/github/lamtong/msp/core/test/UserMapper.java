package io.github.lamtong.msp.core.test;

import io.github.lamtong.msp.core.annotation.Mapper;
import io.github.lamtong.msp.core.mapper.BaseMapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    default String hello(String msg) {
        return "Hello, " + msg;
    }

}
