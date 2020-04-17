package com.yicj.byteio.server;

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

        try {
            bis = new BufferedInputStream(socket.getInputStream()) ;
            pw = new PrintWriter(socket.getOutputStream()) ;
            bos = new BufferedOutputStream(new FileOutputStream(new File("d:/temp/temp1.rar"))) ;
            int r = 0 , t =0 ;
            while (!socket.isInputShutdown() && (r = bis.read(buf)) > 0){
                t += r ;
                log.info("received {}/{} bytes", r ,t);
                bos.write(buf,0 ,r);
                bos.flush();
            }
            log.info("Received {} bytes and done", t);
            pw.println("ok");
        }catch (IOException e){
            log.error("Error on transport", e);
        }finally {
            CommonUtils.close(bos);
            CommonUtils.close(bis);
            CommonUtils.close(pw);
            CommonUtils.close(socket);
        }

    }
}
