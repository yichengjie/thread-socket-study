package com.yicj.service;

import com.yicj.proxy.DynamicInvoker;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class Proxy$UserService implements DynamicInvoker , ApplicationContextAware {

    private Object serviceBean ;
    private ApplicationContext ctx ;

    public void setServiceBean(Object serviceBean) {
        this.serviceBean = serviceBean;
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        this.ctx = ctx ;
    }

    @Override
    public Object invoke(int methodId, Object[] args) throws Throwable {
        if (serviceBean == null){
            serviceBean = ctx.getBean("userService") ;
        }
        UserService userService = (UserService)serviceBean ;
        switch (methodId){
            case 100:
                return userService.helloSC();
            case 101:
                userService.process(2);
                return null;
            default:
                throw new IllegalStateException("Unknown method id".concat(String.valueOf(methodId))) ;
        }
    }

}
