package com.yicj.serialize;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import java.io.*;

public class HessianSerializer implements ObjectSerializer {
    @Override
    public byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream() ;
        HessianOutput ho = new HessianOutput(os) ;
        try {
            ho.writeObject(obj);
            ho.flush();
            return os.toByteArray() ;
        }finally {
            ho.close();
        }
    }

    @Override
    public void serialize(Object obj, OutputStream os) throws IOException {
        HessianOutput ho = new HessianOutput(os) ;
        ho.writeObject(obj);
        ho.flush();
    }


    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException, ClassNotFoundException {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes) ;
        return this.deserialize(is,clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(InputStream is, Class<T> clazz) throws IOException, ClassNotFoundException {
        HessianInput hi = new HessianInput(is) ;
        return (T)hi.readObject();
    }

    @Override
    public Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes) ;
        return this.deserialize(is);
    }

    @Override
    public Object deserialize(InputStream is) throws IOException, ClassNotFoundException {
        HessianInput hi = new HessianInput(is) ;
        return hi.readObject();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, T obj) throws IOException, ClassNotFoundException {
        return (T)deserialize(bytes, obj.getClass());
    }
}
