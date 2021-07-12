package com.swingfrog.summer.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    public static byte[] zip(String name, byte[] bytes) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(buf);
        out.putNextEntry(new ZipEntry(name));
        out.write(bytes);
        out.close();
        return buf.toByteArray();
    }

}
