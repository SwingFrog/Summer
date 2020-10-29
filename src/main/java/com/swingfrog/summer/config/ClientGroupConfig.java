package com.swingfrog.summer.config;

public class ClientGroupConfig {

    /**读写线程数*/
    private int workerThread;
    /**业务线程数*/
    private int eventThread;

    public int getWorkerThread() {
        return workerThread;
    }

    public void setWorkerThread(int workerThread) {
        this.workerThread = workerThread;
    }

    public int getEventThread() {
        return eventThread;
    }

    public void setEventThread(int eventThread) {
        this.eventThread = eventThread;
    }

    @Override
    public String toString() {
        return "ClientGroupConfig{" +
                "workerThread=" + workerThread +
                ", eventThread=" + eventThread +
                '}';
    }

}
