package com.yicj.characterio.server;

import com.yicj.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Server implements Runnable{
    private final ExecutorService threadPool = Executors.newCachedThreadPool() ;
    private ServerSocket serverSocket = null ;

    public void start() throws IOException {
        //创建socket服务端，并绑定port开始监听
        serverSocket = new ServerSocket(9000) ;
        threadPool.execute(this);
    }


    @Override
    public void run() {
        Socket socket = null ;
        //接收来自客户端的连接，并将新连接交给新的线程去处理
        //serverSocket.accept()方法将一直阻塞，直到有新的连接请求进来
        //这会导致服务端在退出（关闭socket）时产生socket close异常
        try {
            while ((socket = serverSocket.accept()) != null){
                log.info("Client {} connect " , socket.getRemoteSocketAddress());
                //用新线程去处理这个新的socket连接，并与客户端进行通信
                threadPool.execute(new Processor(socket));
            }
        }catch (IOException e){
            log.error("Error on processor connection" ,e);
        }
    }

    public void close(){
        //关闭serverSocket和线程池
        if (serverSocket != null && !serverSocket.isClosed()){
            try {
                serverSocket.close();
            } catch (IOException e) {
                log.error("Error on close server socket", e);
            }
        }
        threadPool.shutdown();
    }

    public static void main(String[] args) {
        Server server = new Server() ;
        BufferedReader br = null ;
        try {
            server.start();
            System.out.println("Enter 'exit' to exit");
            br = new BufferedReader(new InputStreamReader(System.in)) ;
            String cmd = null ;
            while ((cmd = br.readLine()) != null){
                if ("exit".equals(cmd)){
                    break;
                }
            }
        }catch (IOException e){
            log.error("Error on run server", e);
        }finally {
            CommonUtils.close(br);
            server.close();
        }

    }
}
