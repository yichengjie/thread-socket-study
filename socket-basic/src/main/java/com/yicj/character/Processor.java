package com.yicj.character;

import com.yicj.utils.StreamUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Slf4j
public class Processor implements Runnable {

    //客户端socket对象
    private Socket socket ;

    public Processor(Socket socket){
        this.socket = socket ;
    }

    @Override
    public void run() {
        BufferedReader br = null ;
        PrintWriter pw = null ;
        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream())) ;
            pw = new PrintWriter(socket.getOutputStream(), true) ;
            //通过socket读写字符串，收到客户端发过来的bye后退出
            while (!Thread.interrupted()){
                String s = br.readLine() ;
                System.out.println(String.format("%s say %s",socket.getRemoteSocketAddress(),s));
                pw.println(s);
                if ("bye".equals(s)){
                    break;
                }
            }
        }catch (IOException e){
            log.error("Error on process command",e);
        }finally {
            StreamUtils.close(br);
            StreamUtils.close(pw);
            StreamUtils.close(socket);
        }

    }
}
