package com.yicj.characterio;

import com.yicj.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Slf4j
public class Client {

    public static void main(String[] args) {

        BufferedReader ir = null ;
        BufferedReader sr = null ;
        String cmd  ;
        PrintWriter pw  ;
        Socket socket = null ;
        try {
            socket = new Socket("127.0.0.1",9000) ;
            //使用BufferedReader从socket读取字符串(读socket输入流)
            sr = new BufferedReader(new InputStreamReader(socket.getInputStream())) ;
            //使用printWrite向socket写入字符串(写socket输出流)
            pw = new PrintWriter(socket.getOutputStream(),true) ;
            //
            System.out.println("Say 'bye' to exit ");
            //读取键盘输入的字符串，冰通过socket发送
            ir = new BufferedReader(new InputStreamReader(System.in)) ;
            while ((cmd = ir.readLine()) != null){
                pw.println(cmd); //发送一行字符串，以行分隔符结尾（请求）
                String s = sr.readLine(); //读取以行分隔符结尾的一行字符（响应）
                System.out.println(String.format("Server say %s", s));
                if ("bye".equals(s)){
                    break;
                }
            }

        } catch (IOException e) {
            log.error("Error on connection",e);
        }finally {
            CommonUtils.close(ir) ;
            CommonUtils.close(sr) ;
            CommonUtils.close(socket) ;
        }
        System.out.println("bye");
    }

}
