package com.yicj.t1;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ThreadLocalDemo implements Runnable {

    protected static final ThreadLocal<String> threadLocal = new ThreadLocal<>() ;

    @Override
    public void run() {
        log.info("start = {} ", threadLocal.get());
        threadLocal.set(Thread.currentThread().getName());
        try {
            test() ;
        }finally {
            threadLocal.remove(); //注释此句试试
        }
    }

    private void test() {
        log.info("test = {}" , threadLocal.get());
    }

    public static void main(String[] args) {
       //注意，此处线程池的大小要小于提交的任务数，这样才能看得出来效果
        ExecutorService es = Executors.newFixedThreadPool(5);
        for (int i = 0 ; i < 20 ; i++){
            ThreadLocalDemo c = new ThreadLocalDemo() ;
            es.execute(c);
        }
        es.shutdown();
        try {
            es.awaitTermination(10L, TimeUnit.SECONDS) ;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
