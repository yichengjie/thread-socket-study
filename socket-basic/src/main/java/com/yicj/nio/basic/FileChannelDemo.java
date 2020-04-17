package com.yicj.nio.basic;

import com.yicj.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

@Slf4j
public class FileChannelDemo {

    private static final String lineSeparator = System.getProperty("line.separator");

    public static void main(String[] args) {
        // RandomAccessFile  raf = null ;
        FileInputStream fis = null ;
        FileOutputStream fos = null ;
        FileChannel fcr = null ;
        FileChannel fcw = null ;
        Charset charset = Charset.forName("UTF-8") ;
        try {
            // raf = new RandomAccessFile("d:/temp/test.txt", "rw") ;
            // FileChannel fc = raf.getChannel() ;
            File f = new File("d:/temp/test.txt") ;
            fis = new FileInputStream(f) ;
            fos = new FileOutputStream(f,true) ;
            fcr = fis.getChannel() ;
            fcw = fos.getChannel() ;
            ByteBuffer buf = ByteBuffer.allocate(1024) ;
            while (fcr.read(buf) >= 0){
                buf.flip() ;
                System.out.println(charset.decode(buf));
                buf.clear();
            }

            buf = charset.encode(CommonUtils.getStdmfDateTime().concat(lineSeparator)) ;
            while (buf.hasRemaining()){
                fcw.write(buf) ;
            }
        }catch (IOException e){
            log.error("Error on read file ", e);
        }finally {
            CommonUtils.close(fcr);
            CommonUtils.close(fcw);
        }
    }

}
