package com.yicj.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 对象序列化接口抽象
 */
public interface ObjectSerializer {


    /**
     * 将指定对象序列化为字节数组
     * @param obj
     * @return
     */
    byte [] serialize(Object obj) throws IOException;

    /**
     * 将指定对象序列化并输出到流
     * @param obj
     * @param os
     * @throws IOException
     */
    void serialize(Object obj, OutputStream os) throws IOException;


    /**
     * 将ByteBuffer反序列化为指定类型的对象(确定改对象是何种类型时)
     * @param buf
     * @param clazz
     * @param <T>
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    //<T> T deserialize(ByteBuffer buf, Class<T> clazz) throws IOException, ClassNotFoundException;

    /**
     * 将字节数组反序列化为指定类型的对象(确定改对象是何种类型时)
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T deserialize(byte [] bytes, Class<T> clazz) throws IOException, ClassNotFoundException;

    /**
     * 从流中读取数据并反序列化为指定对象(确定该对象是何种类型时)
     * @param is
     * @param clazz
     * @param <T>
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    <T> T deserialize(InputStream is , Class<T> clazz) throws IOException, ClassNotFoundException ;

    /**
     * 将字节数组反序列化为指定类型对象(不确定对象类型时)
     * @param bytes
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    Object deserialize(byte [] bytes) throws IOException, ClassNotFoundException ;

    /**
     * 从流中读取数据并反序列化为对象(不确定对象类型时)
     * @param is
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    Object deserialize(InputStream is) throws IOException, ClassNotFoundException ;

    /**
     * 以对象T为模板，反序列化后得到新的T
     * @param bytes
     * @param obj
     * @param <T>
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    <T> T deserialize(byte [] bytes, T obj) throws IOException, ClassNotFoundException ;

}
