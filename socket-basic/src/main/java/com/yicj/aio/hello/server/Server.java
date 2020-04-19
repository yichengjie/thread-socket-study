package com.yicj.aio.hello.server;

import com.yicj.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

@Slf4j
public class Server implements CompletionHandler<AsynchronousSocketChannel,Object> {

    private AsynchronousServerSocketChannel assc = null ;

    public void start() throws IOException {
        //创建服务端socket,开始监听
        assc = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress("127.0.0.1",9000)) ;
        //接收新连接，操作系统会在新连接请求时，调用this的complete方法
        assc.accept(null,this) ;
    }

    public void close(){
        CommonUtils.close(assc);
    }



    @Override
    public void completed(AsynchronousSocketChannel asc, Object attachment) {
        //继续受理一个连接
        assc.accept(null,this) ;
        //为当前通道准备一个reader对象来完成处理，而不是用一个线程来处理
        //此reader拥有一个buffer，并持有当前通道的引用,后面有用
        ReaderCH reader = new ReaderCH(asc) ;
        //对于新建的连接，接收的文件在命名时，编号从0开始
        asc.read(reader.getBuffer(),0 , reader) ;
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        this.close() ;
        log.error("Error on accept connection ", exc);
    }

    public static void main(String[] args) {
        BufferedReader br2 = null ;
        Server server = new Server() ;
        try {
            server.start();
            String cmd = null ;
            System.out.println("Enter 'exit' to exit ");
            br2 = new BufferedReader(new InputStreamReader(System.in)) ;
            while ( (cmd = br2.readLine())!= null ){
                if ("exit".equalsIgnoreCase(cmd)){
                    break;
                }
            }

        }catch (IOException e){
            log.error("Error on run server",e);
        }finally {
            CommonUtils.close(br2);
            server.close();
        }
    }
}
