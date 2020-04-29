package com.swingfrog.summer.db;

public class DaoRuntimeException extends RuntimeException {

    public DaoRuntimeException(String message) {
        super(message);
    }

    public DaoRuntimeException(String format, Object... args) {
        super(String.format(format, args));
    }
}
