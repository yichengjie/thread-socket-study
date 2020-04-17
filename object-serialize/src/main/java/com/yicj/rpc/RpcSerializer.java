package com.yicj.rpc;

import com.yicj.serialize.ObjectSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Iterator;
import java.util.ServiceLoader;

@Slf4j
public class RpcSerializer {

    //为避免耦合，这里不指定使用何种序列化组件，改为第一次使用RpcSerializer时才初始化
    public static ObjectSerializer serializer = null ;

    static {
        //从/META-INF/services/com.yicj.serialize.ObjectSerializer文件装载所有实现类
        ServiceLoader<ObjectSerializer> sl = ServiceLoader.load(ObjectSerializer.class);
        //遍历所有实现类(我们只需要配置1个),取出并使用第一个类作为当前序列化组件
        Iterator<ObjectSerializer> it = sl.iterator();
        if (it.hasNext()){
            serializer = it.next() ;
            log.info("Load object serializer {}", serializer.getClass().getName());
        }
    }


    public static byte [] serialize(RpcCall co) throws IOException {
        return serializer.serialize(co) ;
    }

    public static RpcCall deserialize(byte [] b) throws IOException, ClassNotFoundException {
        return serializer.deserialize(b, RpcCall.class) ;
    }

    public static RpcCall deserialize(RpcCall co ,byte [] b) throws IOException, ClassNotFoundException {
        return serializer.deserialize(b, co) ;
    }


    //注意此方法是非静态的
    public void setSerializer(ObjectSerializer serializer){
        RpcSerializer.serializer = serializer ;
    }

}
