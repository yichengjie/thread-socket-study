package com.yicj.utils;

import java.io.Closeable;
import java.io.IOException;

public class StreamUtils {


    public static void close(Closeable ir) {
        if (ir != null){
            try {
                ir.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
