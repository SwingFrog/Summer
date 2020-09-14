package com.swingfrog.summer.util;

import javax.annotation.Nullable;

public class PasswordUtil {

    public static void convert(byte[] pass, byte[] bytes) {
        int index = bytes.length % 10;
        for (int i = 0; i < bytes.length; i++) {
            if (index >= pass.length)
                index = 0;
            int res = bytes[i] ^ pass[index];
            bytes[i] = (byte)res;
            index++;
        }
    }

    public static void convertForLine(byte[] pass, byte[] bytes) {
        int index = bytes.length % 10;
        for (int i = 0; i < bytes.length; i++) {
            if (index >= pass.length)
                index = 0;
            int res = bytes[i] ^ pass[index];
            if (res != 10 && res != 13)
                bytes[i] = (byte)res;
            index++;
        }
    }

}
