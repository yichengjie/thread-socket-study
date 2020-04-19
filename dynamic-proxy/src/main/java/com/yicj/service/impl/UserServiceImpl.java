package com.yicj.service.impl;

import com.yicj.service.UserService;
import org.springframework.stereotype.Service;

@Service("userService")
public class UserServiceImpl implements UserService {
    @Override
    public void process(int max) {
        for (int i =0 ; i < max ; i ++){
            System.out.println("Hello world");
        }
    }

    @Override
    public String helloSC() {
        return  "hello world";
    }
}
