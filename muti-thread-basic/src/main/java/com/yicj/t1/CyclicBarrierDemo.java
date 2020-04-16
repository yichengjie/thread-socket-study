package com.yicj.t1;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.*;

@Slf4j
public class CyclicBarrierDemo implements Runnable{

    private static final CyclicBarrier barrier = new CyclicBarrier(5) ;
    private int page ;

    public CyclicBarrierDemo(int page){
        this.page = page ;
    }

    @Override
    public void run() {
        for (int i = 0 ; i < 2 ; i++){
            try {
                //模拟不同的处理耗时，最慢的一个线程需要5秒
                Thread.sleep(1000L  + page * 1000L);
                log.info("{} waiting threads", barrier.getNumberWaiting());
                //一旦调用await，就表示当前线程完成了自身的处理，并开始等待其他的线程
                barrier.await(10L, TimeUnit.SECONDS) ;
                log.info("Continue ... ");//注意这句打印的时间
            }catch (InterruptedException e){
                e.printStackTrace();
            }catch (BrokenBarrierException e){
                e.printStackTrace();
            }catch (TimeoutException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ExecutorService es = Executors.newFixedThreadPool(5);
        for(int i = 0 ; i < 5 ; i++){
            CyclicBarrierDemo c = new CyclicBarrierDemo(i) ;
            es.execute(c);
        }
        es.shutdown();
    }
}
