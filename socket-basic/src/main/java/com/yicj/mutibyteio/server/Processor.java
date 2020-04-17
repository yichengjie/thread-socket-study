package com.yicj.mutibyteio.server;

import com.yicj.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;

@Slf4j
public class Processor implements Runnable {
    private Socket socket ;

    public Processor(Socket socket){
        this.socket = socket ;
    }

    @Override
    public void run() {
        BufferedInputStream bis = null ;
        BufferedOutputStream bos = null ;
        PrintWriter pw = null ;
        byte [] buf = new byte[10240] ;
        int i = 0 ;
        //从socket输入流中读取头4个字节，并转换为int，以明确要接收数据长度
        byte [] h = new byte[4] ;
        try {
            bis = new BufferedInputStream(socket.getInputStream()) ;
            pw = new PrintWriter(socket.getOutputStream(),true) ;
            //代表数据长度的4字节非常小，且位于数据包的头部，不会出现拆包现象
            while (!socket.isInputShutdown() && bis.read(h) == 4){
                //将这4字节转成int，表示接下来要接收的数据包大小
                int t = CommonUtils.byteArrayToInt(h) , total = t , r = 0 ;
                if (t <1){
                    pw.println("error");
                    break;
                }
                log.info("Incoming file size = {}", t);
                String fn = String.format("d:/temp/t%d.rar", i);
                bos = new BufferedOutputStream(new FileOutputStream(new File(fn))) ;
                try {
                    //客户端在发送完每个文件后，会等待服务端的响应，所以这里不会越界
                    while (t > 0 &&(r = bis.read(buf)) >0){
                        t -= r ;
                        log.info("Received {} bytes , {} bytes remain. ", r, t);
                        bos.write(buf,0 ,r );
                        bos.flush();
                    }
                    log.info("Received {} bytes as file {}", total, fn);
                    pw.println("ok");
                    i ++ ;
                }finally {
                    CommonUtils.close(bos);
                }
            }
        }catch (IOException e){
            log.error("Error on transport" ,e);
        }finally {
            CommonUtils.close(bis);
            CommonUtils.close(pw);
            CommonUtils.close(socket);
        }
    }
}
