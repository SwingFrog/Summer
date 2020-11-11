package com.swingfrog.summer.event;

public class EventBusRuntimeException extends RuntimeException {

    public EventBusRuntimeException(String message) {
        super(message);
    }

    public EventBusRuntimeException(String format, Object... args) {
        super(String.format(format, args));
    }
}
