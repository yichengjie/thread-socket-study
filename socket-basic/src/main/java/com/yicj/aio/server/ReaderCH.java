package com.yicj.aio.server;

import com.yicj.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;

@Slf4j
public class ReaderCH implements CompletionHandler<Integer,Integer> {
    private AsynchronousSocketChannel asc ;
    private ByteBuffer buffer =  ByteBuffer.allocate(10240) ;
    private FileOutputStream fos ;
    private FileChannel fc ;
    //当前这笔数据的总字节数
    private long total ;
    //当前这笔数据已经接收字节数
    private long received ;

    public ReaderCH(AsynchronousSocketChannel asc){
        this.asc = asc ;
    }

    @Override
    public void completed(Integer r, Integer attachment) {
        if (r <= 0){
            //当客户端调用Socket通道的shutdownOutput时会进到这里
            log.info("No more incoming data now. Quit");
            return;
        }
        received += r ;
        log.info("Read {}/{}/{} bytes", r, received, total);
        try {
            //buffer中已填好数据，只需取出来处理
            buffer.flip() ;
            //fc为空，表示这是一个新文件
            if (fc == null){
                //首8字节记录要传的字节总数(包括这8字节)
                total = buffer.getLong() ;
                //根据当前文件编号生成文件名
                InetSocketAddress isa = (InetSocketAddress) asc.getRemoteAddress() ;
                fos = new FileOutputStream(new File(
                        String.format("d:/temp/%d_%d.rar",isa.getPort(),attachment))) ;
                //打开文件通道，准备写入
                fc = fos.getChannel() ;
            }
            fc.write(buffer) ;//写入（从第9字节开始）
            buffer.clear() ;
            if (received < total){
                //没接收完就继续 read
                asc.read(buffer,attachment,this) ;
            }else {
                //接收完成则发回响应，依次是应发字节总数，已接收成功字节数
                buffer.putLong(total) ;
                buffer.putLong(received) ;
                buffer.flip() ;
                //write后等待I/O完成
                r = asc.write(buffer).get() ;
                log.info("Written response {} bytes", r);
                //重置reader,准备在此通道上的下一次读取
                this.reset() ;
                //读取下一个文件的数据，文件编号+1
                this.asc.read(buffer,attachment + 1, this) ;
            }

        }catch (Exception e){
            log.error("Error on receive file " ,e );
            this.close() ;
        }
    }

    @Override
    public void failed(Throwable exc, Integer attachment) {
        //遇到异常，直接关闭
        this.close() ;
        //打印日志
        SocketAddress sa  = null ;
        try {
            sa = asc.getRemoteAddress() ;
        } catch (IOException e) {
            log.error("Error on getRemoteAddress ",  e);
        }
        log.error("Error on read from {}", sa, exc);
        this.close() ;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
    public void reset(){
        //关闭正在写入的文件，准备读取一个文件
        CommonUtils.close(fos);
        fos = null ;
        CommonUtils.close(fc);
        fc = null ;//重要，新文件传输开始标识
        buffer.clear() ;
        total = 0 ;
        received = 0 ;
    }

    public void close(){
        this.reset();
        CommonUtils.close(asc);
    }
}
