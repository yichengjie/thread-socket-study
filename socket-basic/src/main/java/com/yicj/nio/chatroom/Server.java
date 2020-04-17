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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class Server implements Runnable {

    private static Charset charset = Charset.forName("UTF-8") ;
    private Selector selector = null ;
    private ServerSocketChannel ssc = null ;
    private Thread thread = new Thread(this) ;
    //暂存来自客户端的消息，以便广播发送
    private Queue<String> queue = new ConcurrentLinkedQueue<>() ;
    private volatile boolean live = true;

    public void start() throws IOException {
        selector = Selector.open() ;
        ssc = ServerSocketChannel.open() ;
        ssc.socket().bind(new InetSocketAddress(9000));
        ssc.configureBlocking(false) ;
        ssc.register(selector, SelectionKey.OP_ACCEPT) ;
        thread.start();
    }

    @Override
    public void run() {
        try {
            while (live && !Thread.interrupted()){
                if (selector.select(1000) == 0){
                    continue;
                }
                //遍历通道事件前，先将队列中的消息取出并转成ByteBuffer、以便发给所有通道
                ByteBuffer outBuf = null ;
                String outMsg = queue.poll() ;
                if (CommonUtils.strIsNotEmpty(outMsg)){
                    outBuf = ByteBuffer.wrap(outMsg.getBytes("UTF-8")) ;
                    outBuf.limit(outMsg.length()) ;
                }
                //遍历这些事件（通道）
                Set<SelectionKey> set = selector.selectedKeys();
                Iterator<SelectionKey> it = set.iterator();
                while (it.hasNext()){
                    SelectionKey key = it.next();
                    it.remove();
                    if(key.isValid() && key.isAcceptable()){
                        this.onAcceptable2(key) ;
                    }
                    if (key.isValid() && key.isReadable()){
                        this.onReadable2(key) ;
                    }
                    //如果当前通道可写，并且有消息需要发送，则将消息发送出去
                    if (key.isValid() && key.isWritable() && outBuf != null){
                        SocketChannel sc = (SocketChannel) key.channel() ;
                        this.write(sc, outBuf) ;
                    }
                }
            }
        }catch (IOException e){
            log.info("Error on socket I/O" ,e );
        }
    }

    private void onAcceptable2(SelectionKey key) {
        ServerSocketChannel ssc = (ServerSocketChannel)key.channel() ;
        SocketChannel sc = null ;
        try {
            sc = ssc.accept() ;
            if (sc != null){
                log.info("Client {} connected", sc.getRemoteAddress());
                sc.configureBlocking(false) ;
                //为新建立的连接注册通道可读/通道可写事件
                //并创建一个读取用户的缓冲区（避免重复创建）
                sc.register(selector,SelectionKey.OP_READ | SelectionKey.OP_WRITE, ByteBuffer.allocate(1024)) ;
            }
        }catch (Exception e){
            log.error("Error on accept connection" ,e);
            CommonUtils.close(sc);
        }
    }

    private void onReadable2(SelectionKey key) {
        SocketChannel sc = (SocketChannel)key.channel() ;
        ByteBuffer buf = (ByteBuffer)key.attachment();
        int r = 0 ;
        StringBuilder sb = new StringBuilder() ;
        String rs = null ;
        String remote = null ;
        buf.clear() ;
        try {
            remote = sc.getRemoteAddress().toString() ;
            while ((r = sc.read(buf)) > 0){
                log.info("Received {} bytes from {}",r, remote);
                buf.flip() ;
                sb.append(charset.decode(buf)) ;
                buf.clear() ;
                rs = sb.toString() ;
                //至少读取一行消息(如果发生粘包，则可能读到多行，但不会只读半条消息)
                if (rs.endsWith("\n")){
                    break;
                }
            }
        }catch (Exception e){
            log.error("Error on read socket", e);
            CommonUtils.close(sc);
            return;
        }
        //收到消息后，将消息添加到队列，并在各通道可写入时(writable事件)发送
        if (CommonUtils.strIsNotEmpty(rs)){
            //由于客户端可能连续地发多条消息，因此收取时可能发生粘包，需要切分
            String[] sa = rs.split("\n");
            for(String s : sa){
                if (CommonUtils.strIsNotEmpty(s)){
                    log.info("{}:{}", remote, s);
                    queue.add(String.format("%s:%s\n",remote,s)) ;
                    //如果收到bye，则表示需要关闭当前通道
                    if ("bye".equals(s)){
                        CommonUtils.close(sc);
                    }
                }
            }

        }
    }

    private void write(SocketChannel sc, ByteBuffer buf){
        buf.position(0) ;
        int r = 0 ;
        try {
            while (buf.hasRemaining() && (r = sc.write(buf)) > 0){
                log.info("Write back {} bytes to {}", r , sc.getRemoteAddress());
            }
        }catch (IOException e){
            log.error("Error on write socket", e);
            CommonUtils.close(sc);
            return;
        }
    }

    private void close(){
        live = false ;//告诉thread线程退出
        try {
            thread.join();
        } catch (InterruptedException e) {
            log.error("Be interrupted on join", e);
        }
        CommonUtils.close(selector);
        CommonUtils.close(ssc);
    }

    public static void main(String[] args) {
        BufferedReader br = null ;
        Server server = new Server() ;
        try {
            server.start();
            String cmd2 = null ;
            System.out.println("Enter 'exit' to exit");
            br = new BufferedReader(new InputStreamReader(System.in)) ;
            while ((cmd2 = br.readLine()) != null){
                //退出
                if ("exit".equalsIgnoreCase(cmd2)){
                    break;
                }
            }
        }catch (Exception e){
           log.error("Error on run server", e);
        }finally {
            CommonUtils.close(br);
            server.close(); //优雅的关闭
        }
        System.out.println("bye");
    }

}
