package com.yicj.io.byteio;

import com.yicj.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import java.io.*;
import java.net.Socket;

@Slf4j
public class Client {

    public static void main(String[] args) {
        BufferedReader sr = null ;
        BufferedOutputStream bos = null ;
        BufferedInputStream bis = null ;
        Socket socket = null ;
        byte [] buf = new byte[10240] ;
        try {
            socket = new Socket("127.0.0.1", 9000) ;
            bos = new BufferedOutputStream(socket.getOutputStream()) ;
            bis = new BufferedInputStream(new FileInputStream(new File("d:/temp/temp.rar"))) ;
            int t = bis.available() ;
            int r = 0 ;
            while ((r = bis.read(buf)) > 0){
                t -= r ;
                log.debug("Read {} bytes from the file . {} bytes remain.", r, t);
                bos.write(buf,0, r);
                bos.flush();
            }
            //发送完后关闭输出流，以告知服务端，数据发送完成
            socket.shutdownOutput();
            //
            sr = new BufferedReader(new InputStreamReader(socket.getInputStream())) ;
            String s = sr.readLine();//读取响应流
            if ("ok".equals(s)){
                log.info("Transport the file successfule .");
            }
        }catch (IOException e){
            log.error("Error on connection", e);
        }finally {
            CommonUtils.close(sr);
            CommonUtils.close(bis);
            CommonUtils.close(bos);
            CommonUtils.close(socket);
        }
        System.out.println("done");
    }
}
