package com.yicj.nio.basic;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
public class Buffer1 {

    public static void main(String[] args) {
        ByteBuffer buf1 = ByteBuffer.wrap(new byte[]{1, 2, 3});
        log.info("position: {}" ,buf1.position());
        log.info("limit : {}", buf1.limit());
        log.info("capacity : {}", buf1.capacity());
    }
}
