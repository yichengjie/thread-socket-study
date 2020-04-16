package com.yicj.t1;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class C1_7_3 implements Runnable {
    private static final byte [] flag = new byte[0] ;
    public void run() {
        synchronized (flag){
            try {
                log.info("Before flag.wait()");
                flag.wait();
                log.info("After flag.wait()");
                Thread.sleep(1000L);//换成flag.wait(1000L)试试
                //flag.wait(1000L);
                log.info("After Thread.sleep()");
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(new C1_7_3());
        es.execute(new C1_7_3());
        es.execute(()->{
            try {
                Thread.sleep(1000L);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            synchronized (flag){
                log.info("flag.notifyAll()");
                flag.notifyAll();//换成notify试试
                //flag.notify();
            }
        });
        es.shutdown();
    }
}
