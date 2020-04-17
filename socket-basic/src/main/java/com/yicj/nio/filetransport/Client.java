package com.yicj.nio.filetransport;

import com.yicj.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

@Slf4j
public class Client {


    public static void main(String[] args) {
        SocketChannel sc = null ;
        FileChannel fc = null ;
        try {
            sc = SocketChannel.open() ;
            sc.configureBlocking(true) ;//客户端使用阻塞模式
            sc.connect(new InetSocketAddress("127.0.0.1", 9000)) ;
            if (!sc.finishConnect()){
                log.error("Can not connect to server");
                return;
            }
            //从文件中读取内容，并通过socket发送
            fc = new FileInputStream(new File("d:/temp/temp.rar")).getChannel();
            ByteBuffer buf = ByteBuffer.allocate(10240) ;
            int r = 0 ;
            while ( (r = fc.read(buf)) > 0){
                log.info("Read {} bytes from file , ", r);
                //设置limit为position的值r，再将position设为0
                buf.flip() ;
                //将buf中的内容全部发送出去
                while (buf.hasRemaining() && (r = sc.write(buf)) > 0){
                    log.info("Write {} bytes to server",r);
                }
                buf.clear() ;
            }

            //将读取服务端返回的响应字符串
            while ( (r = sc.read(buf)) > 0){
                log.info("Read {} bytes from socket",r);
            }
            buf.flip() ;
            log.info(Charset.forName("UTF-8").decode(buf).toString());
        } catch (IOException e) {
            log.error("Error on send file ", e );
        }finally {
            CommonUtils.close(fc);
            CommonUtils.close(sc);
        }
        System.out.println("done");
    }
}
