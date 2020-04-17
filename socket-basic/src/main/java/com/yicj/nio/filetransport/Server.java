package com.yicj.nio.filetransport;

import com.yicj.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

@Slf4j
public class Server implements Runnable {

    //多路复用器，用于同时处理多个通道上的多个事件
    private Selector selector = null ;
    //服务端socket通道
    private ServerSocketChannel ssc = null ;
    //工作线程对象
    private Thread thread = new Thread(this) ;
    //工作线程退出标识，需要volatile关键字来保证可见性
    private volatile boolean live = true ;


    public void start() throws IOException {
        //创建多路复用器
        selector = Selector.open() ;
        //创建ServerSocket
        ssc = ServerSocketChannel.open() ;
        //绑定9000端口，开始监听连接请求
        ssc.socket().bind(new InetSocketAddress(9000));
        //使用非阻塞模式
        ssc.configureBlocking(false) ;
        //注册新连接请求事件
        ssc.register(selector, SelectionKey.OP_ACCEPT) ;
        //开启线程，循环处理新的事件
        thread.start();
    }



    @Override
    public void run() {
        try {
            //不断处理Socket通道事件，直到live为false，或者当前线程被中断
            while (live && !Thread.interrupted()){
                //每隔1秒检查所有已注册的通道上是否有我们感兴趣的新事件产生
                if (selector.select(1000) == 0){
                    continue;
                }
                //如果有事件产生，就取出这些事件，并遍历它们
                Set<SelectionKey> set = selector.selectedKeys();
                Iterator<SelectionKey> it = set.iterator();
                while (it.hasNext()){
                    SelectionKey key = it.next();
                    //如果此事件已处理，就需要从现有集合中移除
                    it.remove();
                    //判断是否是一个有效的连接请求事件
                    //注：如果通道被关闭，对应的事件就会失效
                    if (key.isValid() && key.isAcceptable()){
                        this.onAcceptable(key) ;
                    }
                    //判断是否是一个有效的 '通道可读' 事件
                    if (key.isValid() && key.isReadable()){
                        this.onReadable(key) ;
                    }
                    //判断是否是一个有效的'通道可写'事件
                    if (key.isValid() && key.isWritable()){
                        this.onWritable(key) ;
                    }
                }

            }
        }catch (IOException e){
            log.error("Error on socket I/O", e);
        }
    }

    private void onAcceptable(SelectionKey key) throws IOException{
        //处理Acceptable事件时key.channel()返回的是ServerSocketChannel
        ServerSocketChannel ssc =  (ServerSocketChannel)key.channel() ;
        SocketChannel sc = null ;
        try {
            //此方法会立即返回当前可建立的连接，如果没有可建立的连接，将返回null
            sc = ssc.accept() ;
            if (sc != null){
                log.info("Client {} connect", sc.getRemoteAddress());
                //服务端使用非阻塞模式
                sc.configureBlocking(false) ;
                //在新建立的socket通道上注册可读事件，因为在没有读完前，不会像客户端返回应答
                //此外，为每个新建连接都创建一个缓冲区，并作为附件放到SelectionKey，以便后面取用
                sc.register(key.selector(),SelectionKey.OP_READ, ByteBuffer.allocate(10240)) ;
            }
        }catch (Exception e){
            log.info("Error on accept connection ", e);
            CommonUtils.close(sc);//一旦出现问题，关闭当前连接
        }
    }

    private void onReadable(SelectionKey key) throws IOException {
        //处理Readable事件时key.channel()返回的是SocketChannel
        SocketChannel sc = (SocketChannel)key.channel() ;
        //将此前创建并作为附件放入SelectionKey的缓冲区取出来，避免每次都创建一个缓冲区，
        ByteBuffer buf = (ByteBuffer) key.attachment();
        FileChannel fc = null ;
        try {
           //使用当前socket连接的端口号作为文件名
            InetSocketAddress isa = (InetSocketAddress)sc.getRemoteAddress();
            String fn = String.format("d:/temp%d.rar", isa.getPort());
            fc = new FileOutputStream(new File(fn)).getChannel() ;
            int r = 0 ;
            buf.clear() ;
            //循环从Socket通道读取数据，放到buf中
            while ((r = sc.read(buf)) >0){
                log.info("Received {} bytes from {}", r, isa);
                buf.flip() ;//准备读取buf
                r = fc.write(buf) ;//将数据从buf中读出来，写入文件
                log.info("Write {} bytes to file ", r);
                buf.clear() ;//清空buf，以用于下一次读取
            }
            //数据读完后，注册可写事件，并将要发回客户端的应答字符串作为附件
            sc.register(key.selector(), SelectionKey.OP_WRITE, "oc") ;
        }catch (Exception e){
            log.error("Error on read socket", e);
        }finally {
            CommonUtils.close(fc);
        }
    }

    private void onWritable(SelectionKey key) throws IOException{
        //处理writable事件key.channel()返回的是SocketChannel
        SocketChannel sc =  (SocketChannel) key.channel() ;
        //取出前面注册的writable事件时附加的字符串
        String s = (String)key.attachment();
        try {
            //将字符串转换为ByteBuffer
            byte[] ba = s.getBytes("UTF-8");
            ByteBuffer buf = ByteBuffer.wrap(ba);
            //用上面wrap方法得到的缓冲区，其limit为0，需要移动到最后
            buf.limit(ba.length) ;
            int r = 0;
            //将缓冲区中的内容全部发送出去
            while (buf.hasRemaining() && (r = sc.write(buf)) >0){
                log.info("Write {} bytes to {}", r, sc.getRemoteAddress());
            }
        }catch (Exception e){
            log.error("Error on write socket" ,e);
        }finally {
            CommonUtils.close(sc);
        }
    }

    public void close(){
        live = false ;//让线程thread退出循环
        try {
            //让当当前线程（主线程）等待线程thread的退出
            thread.join();
        }catch (InterruptedException e){
            log.error("Be interrupted on join", e);
        }
        //关闭selector和ssc(ServerSocketChannel)
        CommonUtils.close(selector);
        CommonUtils.close(ssc);
    }

    public static void main(String[] args) {
        BufferedReader br = null ;
        Server server = new Server() ;
        try {
            //启动服务器
            server.start();
            //循环读取键盘输入，当用户输入exit时推出程序
            String cmd = null ;
            System.out.println("Enter 'exit' to exit");
            br = new BufferedReader(new InputStreamReader(System.in)) ;
            while ( (cmd = br.readLine()) !=null){
                if("exit".equals(cmd)){
                    break;
                }
            }
        }catch (IOException e){
            log.info("Error on start server ", e);
        }finally {
            CommonUtils.close(br);
            //优雅地关闭服务器，退出事件处理循环，并等待内部线程结束
            server.close() ;
        }
        System.out.println("bye");
    }
}
