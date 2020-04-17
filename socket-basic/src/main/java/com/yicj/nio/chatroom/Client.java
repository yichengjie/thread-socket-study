package com.yicj.nio.chatroom;

import com.yicj.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class Client implements Runnable{

    private static final Charset charset = Charset.forName("UTF-8") ;
    private Selector selector ;
    private SocketChannel socketChannel ;
    private Thread thread = new Thread(this) ;
    //用于读取的buffer
    private ByteBuffer buffer = ByteBuffer.allocate(1024) ;
    //待发送的消息列表
    private Queue<String> queue = new ConcurrentLinkedQueue<>() ;
    private volatile boolean live = true ;

    public void start() throws IOException {
        selector = Selector.open() ;
        socketChannel = SocketChannel.open() ;
        socketChannel.configureBlocking(false) ;
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 9000)) ;
        if (socketChannel.finishConnect()){
            //连接成功后同时注册通道可读可写事件，客户端只关心这两件事
            socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE) ;
            //开启线程，循环处理事件
            thread.start();
        }
    }

    public void close(){
        live = false ;
        try {
            thread.join();
        }catch (InterruptedException e){
            log.error("Be interrupted on join ", e);
        }
        CommonUtils.close(selector);
        CommonUtils.close(socketChannel);
    }

    public boolean isLive(){
        return thread.isAlive() ;
    }

    public void send(String s){
        queue.add(s) ;
    }


    @Override
    public void run() {
        try {
            while (live && !Thread.interrupted()){
                //轮询选取事件，为避免一直阻塞，设置超时时间为1秒
                if (selector.select(1000) ==0){
                    continue;
                }
                //遍历发生的事件，并在处理前或处理后移除
                Set<SelectionKey> set = selector.selectedKeys();
                Iterator<SelectionKey> it = set.iterator();
                while (it.hasNext()){
                    SelectionKey key = it.next();
                    it.remove();//处理事件前移除
                    SocketChannel sc = null ;
                    int r = 0 ;
                    String s = null ;
                    //如果通道可读，且当前事件有效，读取服务端发来的字符串
                    if (key.isValid() && key.isReadable()){
                        sc = (SocketChannel) key.channel() ;
                        StringBuilder sb = new StringBuilder() ;
                        buffer.clear() ;
                        while ( (r = sc.read(buffer)) > 0){
                            log.info("Received {} byte from {}",r , sc.getRemoteAddress());
                            buffer.flip() ;
                            sb.append(charset.decode(buffer)) ;
                            buffer.clear() ;
                            s = sb.toString() ;
                            //如果传入的字符串以换行符结尾，就表示至少已读取到了一条完整消息
                            if (s.endsWith("\n")){
                                break;
                            }
                        }
                        //收到的消息可能发生了粘包，包含多条消息（但不会只有半条），需要切分
                        String[] sa = s.split("\n") ;
                        for (String a : sa){
                            if (CommonUtils.strIsNotEmpty(a)){
                                System.out.println(a);
                            }
                        }
                    }
                    //如果队列中有消息要发送，且通道可写，就立即发送
                    if (key.isValid() && key.isWritable() && !queue.isEmpty()){
                        s = queue.poll() ;
                        sc = (SocketChannel)key.channel() ;
                        ByteBuffer buf = ByteBuffer.wrap(s.getBytes("UTF-8")) ;
                        buf.limit(s.length()) ; // wrap后，buf的limit为0，需要修改
                        while (buf.hasRemaining() && (r = sc.write(buf)) > 0){
                            log.info("Write {} bytes to server", r);
                        }
                    }
                }
            }
        }catch (IOException e) {
            log.error("Error on socket I/O");
        }finally {
            CommonUtils.close(selector);
            CommonUtils.close(socketChannel);
        }
    }

    public static void main(String[] args) {
        BufferedReader ir = null ;
        Client client = new Client() ;
        try {
            //启动客户端通信线程
            client.start();
            //接收用户的输入，并通过客户端通信线程发送出去
            String cmd = null ;
            ir = new BufferedReader(new InputStreamReader(System.in)) ;
            System.out.println("Say 'bye' to exit");
            while ( (cmd = ir.readLine()) != null && client.isLive()){
                if (CommonUtils.strIsNotEmpty(cmd)){
                    //将‘消息’放入队列，等待发送
                    client.send(cmd.concat("\n"));
                    if ("bye".equals(cmd)){
                        client.close();
                        break;
                    }
                }
            }
        }catch (IOException e){
            log.info("Error on run client" ,e);
        }finally {
            CommonUtils.close(ir);
            client.close();
        }
        System.out.println("done");
    }
}
