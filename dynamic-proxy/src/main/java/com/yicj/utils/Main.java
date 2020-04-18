package com.yicj.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

public class Main {

    public static void main(String[] args) throws Exception{

        //创建自定义类装载器
        MyClassLoader cl = new MyClassLoader() ;
        //申明一个足够大的字节码数组来接收要装载的字节
        byte [] ba = new byte[1024] ;
        InputStream is = null ;
        int r = 0 ;
        try {
            //读取.class文件中的字节码到数组
            is = new FileInputStream("d:/temp/My.class") ;
            r = is.read(ba) ;
        }finally {
            CommonUtils.close(is);
        }
        //使用自定义类装载器将字节码转移为对应的class对象
        //Class<?> clazz = cl.defineMyClass(null, ba, 0, r);
        //一旦定义好Class，就可以通过ClassLoader的loaderClass方法获取
        cl.defineMyClass(null, ba, 0, r) ;
        Class<?> clazz = cl.loadClass("com.yicj.test.My");
        //测试装载是否成功
        System.out.println(clazz.getCanonicalName());
        //实例化
        Object o = clazz.newInstance();
        //使用反射调用此对象的test方法
        Method m = clazz.getMethod("test") ;
        m.invoke(o) ;
    }
}
