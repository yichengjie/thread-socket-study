package com.yicj.aio.hello;

import com.yicj.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
public class Client implements CompletionHandler<Integer, Integer> {

    //异步Socket通道
    private AsynchronousSocketChannel asc = null ;
    //读写公用的缓冲区
    private ByteBuffer buffer = ByteBuffer.allocate(10240) ;
    //当前要读取的文件输入流
    private FileInputStream fis ;
    //当前要读取的文件通道
    private FileChannel fc ;
    //待发送的文件最大编号
    private int maxFileNo = 2 ;

    public void start() throws IOException, ExecutionException, InterruptedException {
        //创建Socket通道
        asc = AsynchronousSocketChannel.open() ;
        //建立连接，用Future模式等待连接建立
        Future<Void> ft = asc.connect(new InetSocketAddress("127.0.0.1", 9000));
        ft.get() ;
        //从第0个文件开始发送，这里也可以将文件集合的iterator作为参数
        this.send(0) ;
    }

    public void send(Integer i) throws IOException {
        //此方法会被反复调用，发送新文件前，需要先确保上一个文件已被关闭
        CommonUtils.close(fc);
        CommonUtils.close(fis);
        //读取当前文件，参数i为当前要读取的文件编号
        String fn = String.format("d:/temp/s%d.rar", i);
        fis = new FileInputStream(new File(fn)) ;
        fc = fis.getChannel() ;
        //首次读取时，读取文件大小，并作为前8个字节发送
        buffer.clear() ;
        //要发送的字节数为文件大小+ 文件长度常量（Long型）所占8字节发送
        buffer.putLong(fc.size() +8) ;
        //将文件内容读取到buffer，一次读不完，就等待这次发送出去后继续读取和发送
        fc.read(buffer) ;
        //将buffer的limit设置为当前position（有效字节数），position设置为0
        buffer.flip() ;
        //异步发送，注意观察日志中的线程号
        log.info("Write first buffer of file {}",fn);
        //将文件号作为附件，这里也可以将文件集合作为iterator作为附件
        asc.write(buffer,i, this) ;
    }

    public void close(){
        CommonUtils.close(asc);
        CommonUtils.close(fis);
        CommonUtils.close(fc);
    }


    @Override
    public void completed(Integer r, Integer attachment) {
        if (r <= 0){
            log.info("No written data now. Quit");
            return;
        }
        //注意观察日志中的线程号
        log.info("Written {} bytes" ,r);
        try {
            //上次发送可能没发完，需要继续发送
            if (buffer.hasRemaining()){
                asc.write(buffer,attachment,this) ;
                return;
            }
            //也可以不发送剩余的数据,在compact后继续使用
            //buffer.compact() ;
            buffer.clear() ;
            //将文件内容读取到缓冲区
            if (fc.read(buffer) >0){
                //将buffer翻转，写入socket通道，发送出去
                buffer.flip() ;
                asc.write(buffer,attachment,this) ;
            }else {
                //读取不到则说明已遇到文件尾，表示发送完成，这时可以读取服务端的响应了
                r = asc.read(buffer).get() ;
                log.info("Read response {} bytes", r);
                //读取服务端的响应，依次是应发字节数、已接收完成的字节数
                buffer.flip() ;
                long total = buffer.getLong();
                long received = buffer.getLong() ;
                System.out.println(String.format("%d/%d",total, received));
                //发送下一个文件，这里也可以将文件集合的iterator作为附件
                if (attachment < maxFileNo){
                    this.send(attachment +1);
                }else {
                    //关闭输出流，告诉服务器，数据已发送完成
                    asc.shutdownOutput() ;
                    //通知主线程继续执行(退出)
                    synchronized (this){
                        this.notify();
                    }
                }
            }
        }catch (Exception e){
            log.info("Error on send file ");
            this.close() ;
        }
    }

    @Override
    public void failed(Throwable exc, Integer attachment) {
        //遇到异常，直接关闭
        this.close() ;
        //打印日志
        SocketAddress sa = null ;
        try {
            sa = asc.getRemoteAddress() ;

        }catch (IOException e){
            log.error("Error on getRemoteAddress",e);
        }
        log.error("Error on read from {}", sa, exc);
    }


    public static void main(String[] args) {
        Client client = new Client() ;
        try {
            client.start();
            //阻塞主线程，等待client完成传输并唤醒自己
            synchronized (client){
                client.wait();
            }
        } catch (Exception e) {
            log.error("Error on run client", e);
        } finally {
            client.close();
        }
        System.out.println("bye");

    }
}
