package com.yicj.nio.basic;

import sun.security.action.GetPropertyAction;

import java.security.AccessController;

public class Channel1 {
    private static final String lineSeparator =
            AccessController.doPrivileged(
                    new GetPropertyAction("line.separator")) ;
    public static void main(String[] args) {
        System.out.println(lineSeparator);

    }
}
