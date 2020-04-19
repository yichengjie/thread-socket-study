package com.yicj.compile;

import com.yicj.utils.MyClassLoader;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.nio.CharBuffer;

/**
 * 本类时对java compile api的封装
 */
public class MemJavaMan extends ForwardingJavaFileManager {

    // 这里也可以选择直接实现JavaFileManager接口，实现每个方法时直接调用内部这个
    // javaFileManager的对应方法即可
    // implements JavaFileManager{
    // 获取当前系统中的Java编译器
    private static JavaCompiler compiler = ToolProvider.getSystemJavaCompiler() ;
    // Java文件管理器，第一个参数为编译诊听器，用于接收编译中产生的事件报告
    private static JavaFileManager javaFileManager = compiler.getStandardFileManager(null, null, null) ;
    // 在构造本类时，传入自定义ClassLoader，要求必须实现MyClassLoader接口，以支持类注册
    private MyClassLoader classLoader ;
    //用来封装Java源码文件的类,JavaFileManager会调用此类的getCharContent()来获取Java源码
    class JavaScource extends SimpleJavaFileObject{

        private String source ;
        /**
         *
         * @param className
         * @param source
         */
        public JavaScource(String className, String source) {
            super(URI.create(String.format("string:///%s%s",
                    className.replace(".", "/"),
                    Kind.SOURCE.extension)), Kind.SOURCE) ;
            this.source = source ;
        }

        //JavaFileManager会调用此方法来获取源码，它不会关心源码来自哪里


        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return CharBuffer.wrap(source) ;
        }

    }

    //一个JavaClazz会对应一份字节码，但一份Java源码会对应多个JavaClazz
    class JavaClazz extends SimpleJavaFileObject{

        private String className ;

        //保存className，以便在字节码生成后能立即转为Class
        protected JavaClazz(String className) {
            super(URI.create(String.format("string:///%s%s",
                    className.replace(".", "/"),
                    Kind.CLASS.extension)), Kind.CLASS) ;
            System.out.println("JavaClazz : " + className);
            this.className = className ;
        }

        /**
         * JavaFileManager会调用此方法来输出编译好的字节码
         * @return
         * @throws IOException
         */
        @Override
        public OutputStream openOutputStream() throws IOException {
            //将原本输出到文件的流输出到字节数组中，避免生成.class文件
            return new FilterOutputStream(new ByteArrayOutputStream()){
                @Override
                public void close() throws IOException {
                    //关闭流后，立即将得到的字节码转为Class对象
                    System.out.println("OnClose : " + className);
                    out.close();
                    ByteArrayOutputStream os = (ByteArrayOutputStream) out ;
                    byte[] b = os.toByteArray();
                    classLoader.defineMyClass(className, b, 0, b.length) ;
                }
            } ;
        }
    }


    public MemJavaMan(){
        super(javaFileManager);
        classLoader = new MyClassLoader() ;
    }

    public MemJavaMan(JavaFileManager javaFileManager) {
        super(javaFileManager);
        MemJavaMan.javaFileManager = javaFileManager ;
        classLoader = new MyClassLoader() ;
    }

    public MemJavaMan(MyClassLoader classLoader){
        super(javaFileManager);
        this.classLoader = classLoader ;
    }

    public MemJavaMan(JavaFileManager javaFileManager, MyClassLoader classLoader){
        super(javaFileManager);
        MemJavaMan.javaFileManager = javaFileManager ;
        this.classLoader = classLoader ;
    }

    //未写完保存



}
