package com.yicj.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

public class KryoSerializer implements ObjectSerializer {

    //用ThreadLocal来为每个线程持有一个kryo对象，使用软应用的目的是容许垃圾收集器回收它
    protected ThreadLocal<SoftReference<Kryo>> threadLocal = new ThreadLocal<>() ;


    protected Kryo getKryo(){
        SoftReference<Kryo> ref = threadLocal.get();
        Kryo kryo = null ;
        if (ref == null || (kryo = ref.get()) == null){
            kryo = new Kryo() ;
            //默认为true,支持对象引用于循环引用情况
            //kryo.setReferences(true) ;
            //默认为false表示无需预先注册类
            //kryo.setRegistrationRequired(false);
            //注册已知类
            this.register(kryo) ;
            threadLocal.set(new SoftReference<>(kryo));
        }else {
            kryo.reset();
        }
        return kryo ;
    }

    private void register(Kryo kryo) {
        //注册一些用到的常用类
        kryo.register(HashMap.class) ;
        kryo.register(ArrayList.class) ;
    }


    @Override
    public byte[] serialize(Object obj) throws IOException {
        Output output = new Output(4096, -1) ;
        try {
            //this.getKryo().writeObject(output,obj);
            //为了最大程度兼容，序列化时必须写入class信息,反序列化时必须读取class信息
            this.getKryo().writeClassAndObject(output,obj);
            output.flush();
            return output.toBytes() ;
        }finally {
            output.close();
        }
    }

    @Override
    public void serialize(Object obj, OutputStream os) throws IOException {
        Output output = new Output(os) ;
        this.getKryo().writeClassAndObject(output,obj);
        output.flush();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException, ClassNotFoundException {
        Input input = new Input(bytes) ;
        //为了最大程度兼容，序列化时必须写入class信息，反序列化时必须读取class信息
        return (T)this.getKryo().readClassAndObject(input);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(InputStream is, Class<T> clazz) throws IOException, ClassNotFoundException {
        Input input = new Input(is) ;
        return (T)this.getKryo().readClassAndObject(input);
    }

    @Override
    public Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        Input input = new Input(bytes) ;
        return this.getKryo().readClassAndObject(input);
    }

    @Override
    public Object deserialize(InputStream is) throws IOException, ClassNotFoundException {
        Input input = new Input(is) ;
        return this.getKryo().readClassAndObject(input);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, T obj) throws IOException, ClassNotFoundException {
        return (T)deserialize(bytes,obj.getClass());
    }
}
