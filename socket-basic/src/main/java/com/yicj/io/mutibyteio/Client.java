package com.yicj.io.mutibyteio;

import com.yicj.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;

@Slf4j
public class Client {

    public static void main(String[] args) {
        BufferedReader sr = null ;
        BufferedInputStream bis = null ;
        BufferedOutputStream bos = null ;
        byte [] buf = new byte[10240] ;
        Socket socket = null ;
        try {
            socket = new Socket("127.0.0.1", 9000) ;
            //使用字符流方式读取响应数据
            sr = new BufferedReader(new InputStreamReader(socket.getInputStream())) ;
            //使用BufferedOutputStream实现基于二进制字节流的数据发送
            bos = new BufferedOutputStream(socket.getOutputStream()) ;
            //依次向服务端发送3个文件，每次发送一个文件，服务器均将回应一个ok表示接收完成
            for (int i = 0 ; i <3; i ++){
                String fn = String.format("d:/temp/s%d.rar",i) ;
                //从文件中读取字节流
                bis = new BufferedInputStream(new FileInputStream(new File(fn))) ;
                try {
                    //读取文件大小，并转换为4个字节来发送，以告知远端要接收的数据长度
                    int t = bis.available(), r = 0 ;
                    bos.write(CommonUtils.intToByteArray(t)); // 将int转换为字节数组
                    while ((r = bis.read(buf)) >0){
                        t -= r ;
                        log.info("Read {} bytes from file , {} bytes remain.", r, t);
                        //读到的字节可能不足以填满buf，只将有效的字节写到文件中
                        bos.write(buf, 0, r);
                        bos.flush();
                    }
                    String s = sr.readLine() ;
                    if (s.equals("ok")){
                        log.info("Transport file {} successfully .", fn);
                    }else {
                        log.info("Transport file {} failed .", fn);
                    }
                }finally {
                    CommonUtils.close(bis);
                }
            }
            //文件都发送完毕后，关闭输出流，告知对端，数据发送完了
            socket.shutdownOutput();
        }catch (IOException e){
            log.error("Error on connection" ,e);
        }finally {
            CommonUtils.close(sr);
            CommonUtils.close(bos);
            CommonUtils.close(socket);
        }
        System.out.println("done");
    }

}
