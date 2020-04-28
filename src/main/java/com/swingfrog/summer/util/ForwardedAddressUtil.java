package com.swingfrog.summer.util;

public class ForwardedAddressUtil {

    public static final String KEY = "X-Forwarded-For";

    public static String parse(String forwardedAddressList) {
        if (forwardedAddressList == null || forwardedAddressList.isEmpty())
            return null;
        return forwardedAddressList.split(",")[0];
    }

}
