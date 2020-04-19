package com.yicj.proxy;

import org.springframework.context.ApplicationContext;

import java.util.concurrent.atomic.AtomicInteger;

public interface DynamicInvoker {
    //用于全局服务方法调用计数，调用发起前为+1，调用完成后为-1，也用于优雅停机
    AtomicInteger count = new AtomicInteger(0) ;

    /**
     * 调用本地服务方法
     * @param methodId  本地方法id
     * @param args   方法调用参数
     * @return
     * @throws Throwable
     */
     Object invoke(int methodId, Object[] args) throws Throwable ;

    /**
     * DynamicInvoker需要通过Spring容器获取要执行调用的本地bean
     * @param ctx Spring容器
     */
     void setApplicationContext(ApplicationContext ctx) ;

}
