package com.yicj.utils;

public class MyClassLoader extends ClassLoader {

    /**
     * ClassLoader的defineMyClass方法是protected的，不能被直接访问，
     * 通过继承此ClassLoader，可以将此方法改为public的，以暴漏出来
     * @param name 类名。字节码中包含类名，这里传null即可
     * @param b  字节码
     * @param off 从表数组的哪一位开始读取
     * @param len 字节码的长度
     * @return
     */
    public Class<?> defineMyClass(String name, byte [] b, int off, int len){
        return super.defineClass(name, b, off, len) ;
    }

}
