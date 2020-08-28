package com.swingfrog.summer.util;

public class ThreadCountUtil {

    public static int ioDenseness(int threadCount) {
        if (threadCount <= 0) {
            threadCount = Runtime.getRuntime().availableProcessors() * 2;
        }
        return threadCount;
    }

    public static int cpuDenseness(int threadCount) {
        if (threadCount <= 0) {
            threadCount = Runtime.getRuntime().availableProcessors() + 1;
        }
        return threadCount;
    }

}
