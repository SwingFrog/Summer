package com.swingfrog.summer.util;

public class ThreadCountUtil {

    public static int convert(int threadCount) {
        if (threadCount <= 0) {
            threadCount = Runtime.getRuntime().availableProcessors() * 2;
        }
        return threadCount;
    }

}
