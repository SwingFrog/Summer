package com.swingfrog.summer.ecs;

public class EcsRuntimeException extends RuntimeException {

    public EcsRuntimeException(String message) {
        super(message);
    }

    public EcsRuntimeException(String format, Object... args) {
        super(String.format(format, args));
    }

}
