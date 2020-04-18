package com.yicj.proxy;

import com.yicj.service.Service;
import com.yicj.service.impl.ServiceImpl;
import javassist.*;

public class JavassistHello {

    public static void main(String[] args) throws Exception {

        String clsName = "DynaServiceImpl";
        String pkgName = JavassistHello.class.getPackage().getName() ;
        //获得默认的类池
        ClassPool clsPool = ClassPool.getDefault();
        //添加classpath
        clsPool.insertClassPath(new ClassClassPath(JavassistHello.class)) ;
        //申明包
        clsPool.importPackage(pkgName);
        //引用Service，ServiceImpl
        clsPool.importPackage(Service.class.getPackage().getName());
        //
        //
        //拼装类全名，动态创建类
        CtClass ctCls = clsPool.makeClass(String.format("%s.%s",pkgName,clsName)) ;
        //为这个类添加要实现的接口
        ctCls.addInterface(clsPool.getCtClass(Service.class.getName()));
        //
        //
        //添加一个字段
        CtField svcField = CtField.make("private Service service ; \n", ctCls) ;
        ctCls.addField(svcField);
        //
        //
        //为字段key添加getter和setter
        ctCls.addMethod(CtNewMethod.getter("getService", svcField));
        ctCls.addMethod(CtNewMethod.setter("setService", svcField));
        //
        //
        //添加构造函数
        CtConstructor ctConstructor = new CtConstructor(new CtClass[]{clsPool.getCtClass(Service.class.getName())}, ctCls) ;
        //为构造函数设置函数体，用$1代替第一个参数
        ctConstructor.setBody("{\n this.service = $1 ; \n}");
        //把构造函数添加到新的类中
        ctCls.addConstructor(ctConstructor);
        //
        //
        //添加自定义方法
        CtMethod test = new CtMethod(CtClass.voidType, "test", new CtClass[]{}, ctCls) ;
        //为自定义方法设置修饰符
        test.setModifiers(Modifier.PUBLIC);
        //为自定义方法设置函数体
        test.setBody("{\n System.out.println(this.getClass().getName()) ; \n}");
        ctCls.addMethod(test);
        //
        //
        //或者直接根据字符串来创建方法
        StringBuilder sb = new StringBuilder() ;
        sb.append("public void process(int max) {\n") ;
        sb.append("   System.out.println(\"Before invoking ...\") ; \n") ;
        sb.append("   service.process(max) ; \n") ;
        sb.append("   System.out.println(\"After imvoking ...\") ; \n") ;
        sb.append("}") ;
        CtMethod process = CtMethod.make(sb.toString(), ctCls) ;
        process.setModifiers(Modifier.PUBLIC);
        ctCls.addMethod(process);
        //
        //
        //为了验证效果，下面使用反射执行方法的printInfo
        Class<?> clazz = ctCls.toClass();
        //通过构造函数创建此对象，为ServiceImpl对象创建代理
        Object obj = clazz.getConstructor(Service.class).newInstance(new ServiceImpl());
        //通过反射调用上面的test方法
        obj.getClass().getMethod("test", new Class[]{}).invoke(obj) ;
        //也可以转为对应的接口，在调相应的接口方法
        ((Service)obj).process(2);
    }
}
