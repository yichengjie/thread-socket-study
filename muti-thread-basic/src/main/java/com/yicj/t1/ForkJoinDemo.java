package com.yicj.t1;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

@Slf4j
public class ForkJoinDemo extends RecursiveTask<Integer> {

    protected int start ;
    protected int end ;

    public ForkJoinDemo(int start, int end){
        this.start = start ;
        this.end = end ;
    }

    @Override
    protected Integer compute() {
        //申明计算用的变量
        int m = 2000 ;
        int s = start ;
        int n = end ;
        int r = 0 ;
        //创建子任务，每个子任务处理m个数字
        List<ForkJoinTask<Integer>> lt = new ArrayList<>() ;
        do {
            if(n - s < m * 1.5f){
                for (int i = s ; i <= n ;i++){
                    r += i ;
                }
                log.info("sum : {} , {} = {}" , s , n , r);
            }else {
                n = Math.min(s +m -1 , n) ;
                lt.add(new ForkJoinDemo(s, n).fork()) ;
            }
            s = n +1 ;
            n = end ;
        }while (s <= n) ;
        //合并子任务处理结果
        for(ForkJoinTask<Integer> t : lt){
            r += t.join() ;
        }
        return r ;
    }

    public static void main(String[] args) {
        ForkJoinPool fjp = new ForkJoinPool() ;
        //计算从ss到nn的累加
        int ss = 1 , nn = 10001 ;
        ForkJoinTask<Integer> result = fjp.submit(new ForkJoinDemo(ss, nn));
        try {
            System.out.println(result.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
