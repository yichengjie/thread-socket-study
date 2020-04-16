package com.yicj.t1;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CowntDownLatchDemo {
    public static void main(String[] args) {
        int count = 200 ;
        //初始化一个200大小的计数器
        final CountDownLatch cdl = new CountDownLatch(count) ;
        ExecutorService es = Executors.newFixedThreadPool(5);
        for(int i = 0 ; i < count ; i ++){
            es.execute(() -> {
                try {
                    System.out.println(cdl.getCount());
                } finally {
                    cdl.countDown();//计算器减1
                }
            });
        }
        try {
            cdl.await(10L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        es.shutdown();
        System.out.println(cdl.getCount());
    }
}
