package com.yicj.rpc;

import org.junit.Test;
import java.io.IOException;


public class RpcSerializerTest {

    @Test
    public void serialize(){
        RpcCall ro1 = new RpcCall("张三") ;
        try {
            byte[] bytes = RpcSerializer.serialize(ro1);
            RpcCall ro2 = RpcSerializer.deserialize(bytes);
            System.out.println(ro2);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}