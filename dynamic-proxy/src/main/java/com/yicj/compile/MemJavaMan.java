package com.yicj.compile;

import com.yicj.utils.MyClassLoader;

import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.ToolProvider;

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



    /**
     * Creates a new instance of ForwardingJavaFileManager.
     *
     * @param fileManager delegate to this file manager
     */
    protected MemJavaMan(JavaFileManager fileManager) {
        super(fileManager);
    }



}
