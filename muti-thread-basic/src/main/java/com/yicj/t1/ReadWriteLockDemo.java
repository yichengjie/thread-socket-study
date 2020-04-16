package com.yicj.t1;

import lombok.extern.slf4j.Slf4j;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//这个结果有点意外
@Slf4j
public class ReadWriteLockDemo {

    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock() ;
    private static Deque<Integer> data = new LinkedList<>();
    private static int value;

    public static void main(String[] args) throws InterruptedException {
        ExecutorService es = Executors.newCachedThreadPool();
        for (int i = 0; i < 5; i++) {
            es.execute(new Runnable() {
                @Override
                public void run() {
                    //每个写线程向队列头添加10个数字
                    for (int i = 0; i < 10; i++) {
                        //分别用readLock和writeLock的Lock方法试试
                        lock.writeLock().lock();
                        try {
                            data.addFirst(++value);
                        } finally {
                            lock.writeLock().unlock();
                        }
                    }
                }
            });
            es.execute(new Runnable() {
                @Override
                public void run() {
                    //每个写线程从队列尾读取10个字符
                    for (int i = 0; i < 10; i++) {
                        Integer v ;
                        do {
                            //分别用readLock和writeLock得loca方式试试
                            lock.readLock().lock();
                            //lock.writeLock().lock();
                            try {
                                v = data.pollLast() ;
                                if (v == null) {
                                    Thread.yield();
                                }else {
                                    log.info("read value : {}" , value);
                                    break;
                                }
                            } finally {
                                lock.readLock().unlock();
                                //lock.writeLock().unlock();
                            }
                        } while (true);
                        //log.info("read value : {}" , value);
                    }
                }
            });
        }
        es.shutdown();
        es.awaitTermination(10L, TimeUnit.SECONDS) ;

        log.info("-----------real order-------------" + data.size());
        for (Integer i : data){
            System.out.println(i);
        }
    }




}
