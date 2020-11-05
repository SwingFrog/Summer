package com.swingfrog.summer.server.exception;

public class RemoteRuntimeException extends RuntimeException {

    public RemoteRuntimeException(String message) {
        super(message);
    }

    public RemoteRuntimeException(String format, Object... args) {
        super(String.format(format, args));
    }
}
