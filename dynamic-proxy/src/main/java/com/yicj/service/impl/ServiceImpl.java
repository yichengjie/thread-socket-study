package com.yicj.service.impl;

import com.yicj.service.Service;

public class ServiceImpl implements Service {
    @Override
    public void process(int max) {
        for (int i =0 ; i < max ; i ++){
            System.out.println("Hello world");
        }
    }
}
