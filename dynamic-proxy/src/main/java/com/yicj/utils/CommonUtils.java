package com.yicj.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;

@Slf4j
public class CommonUtils {

    public static void close(Closeable p) {
        if (p != null){
            try {
                p.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.error("Error on close {}", p.getClass().getName(), e);
            }
        }
    }

    /**
     * int 转字节数组
     * @param a
     * @return
     */
    public static byte[] intToByteArray(int a){
        return new byte[]{
                (byte) ((a >> 24) & 0xFF) ,
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) ((a & 0xFF))
        } ;
    }

    /**
     * 字节数组转int
     * @param b
     * @return
     */
    public static int byteArrayToInt(byte [] b){
       return b[3] & 0xFF |
               (b[2] & 0xFF) << 8 |
               (b[1] & 0xFF) << 16 |
               (b[0] & 0xFF) << 24 ;
    }

    public static void main(String[] args) {
        int a = 7 ;
        byte[] bytes = intToByteArray(a);
        int i = byteArrayToInt(bytes);
        System.out.println(i);
    }

    public static boolean strIsNotEmpty(String str){
        if (str == null || str.trim().length() == 0){
            return false ;
        }
        return true ;
    }


    public static String getStdmfDateTime(){
        return "20200417" ;
    }
}
