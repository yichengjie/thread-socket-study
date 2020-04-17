package com.yicj.serialize;

import org.nustaq.serialization.FSTConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class FastSerializer implements ObjectSerializer {

    //全局共享的配置
    public static final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration() ;

    static {
        //不能注册这个类，会报NoClassDefException
        //conf.registerClass(InvocationHandler.class);
        conf.registerClass(HashMap.class);
        conf.registerClass(ArrayList.class);
    }


    @Override
    public byte[] serialize(Object obj) throws IOException {
        //线程安全的快捷序列化方式
        return conf.asByteArray(obj) ;
    }

    @Override
    public void serialize(Object obj, OutputStream os) throws IOException {
        //线程安全的快捷序列化方式
        conf.getObjectOutput(os).writeObject(obj);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException, ClassNotFoundException {
        //线程安全的工厂模式序列化
        return (T)conf.getObjectInput(bytes).readObject();
    }

    @Override
    public <T> T deserialize(InputStream is, Class<T> clazz) throws IOException, ClassNotFoundException {

        return (T)this.serialize(is);
    }

    @Override
    public Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        //线程安全的工厂模式序列化
        return conf.getObjectInput(bytes).readObject();
    }

    @Override
    public Object deserialize(InputStream is) throws IOException, ClassNotFoundException {

        return conf.getObjectInput(is).readObject();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, T obj) throws IOException, ClassNotFoundException {
        return (T)deserialize(bytes,obj.getClass());
    }
}
