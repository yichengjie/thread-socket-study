package com.yicj.service;

import com.yicj.config.AppConfig;
import com.yicj.proxy.DynamicInvoker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class UserServiceTest {
    AnnotationConfigApplicationContext ctx ;
    @Before
    public void before(){
        ctx = new AnnotationConfigApplicationContext();
        ctx.register(AppConfig.class);
        ctx.refresh();
    }

    @Test
    public void test(){
        Object userService = ctx.getBean("userService");
        System.out.println(userService);
    }


    @Test
    public void testInvoke100() throws Throwable {
        DynamicInvoker invoker = ctx.getBean(Proxy$UserService.class);
        Object ret = invoker.invoke(100, new Object[]{}) ;
        System.out.println(ret);
    }

    @Test
    public void testInvoke101() throws Throwable {
        DynamicInvoker invoker = ctx.getBean(Proxy$UserService.class);
        Object ret = invoker.invoke(101, new Object[]{2}) ;
        System.out.println(ret);
    }

    @After
    public void after(){
        ctx.close();
    }
}
