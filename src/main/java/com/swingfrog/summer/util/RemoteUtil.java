package com.swingfrog.summer.util;

public class RemoteUtil {

    public static String mergeRemoteMethod(String remote, String method) {
        if (method == null || method.isEmpty())
            return remote;
        if (remote == null || remote.isEmpty())
            return method;
        String merge = remote + "." + method;
        return merge.intern();
    }

}
