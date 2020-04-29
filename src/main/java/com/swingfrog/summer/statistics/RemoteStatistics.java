package com.swingfrog.summer.statistics;

import com.google.common.collect.Maps;
import com.swingfrog.summer.protocol.SessionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class RemoteStatistics {

    private static final Logger log = LoggerFactory.getLogger(RemoteStatistics.class);

    private static volatile boolean open = true;
    private String exportDir = "statistics";
    private ConcurrentMap<SessionRequest, Req> requestMap = Maps.newConcurrentMap();
    private ConcurrentMap<String, Statistics> remoteMethodMap = Maps.newConcurrentMap();

    private static class Req {
        private long time;
        private int size;
    }
    private static class Value {
        private int min = Integer.MAX_VALUE;
        private int max = Integer.MIN_VALUE;
        private int total;
        private int count;
        private void add(int current) {
            min = Math.min(min, current);
            max = Math.max(max, current);
            total += current;
            count ++;
        }
        private int avg() {
            return total / count;
        }
    }
    private static class Statistics {
        private Value consumeMs = new Value();
        private Value reqSize = new Value();
        private Value respSize = new Value();
    }

    private static class SingleCase {
        public static final RemoteStatistics INSTANCE = new RemoteStatistics();
    }

    private RemoteStatistics() {
    }

    private void setExportDir(String exportDir) {
        this.exportDir = exportDir;
    }

    private void add(SessionRequest request, int reqSize) {
        Req req = new Req();
        req.time = System.currentTimeMillis();
        req.size = reqSize;
        requestMap.putIfAbsent(request, req);
    }

    private void remove(SessionRequest request) {
        requestMap.remove(request);
    }

    private void finishRemove(SessionRequest request, int respSize) {
        Req req = requestMap.remove(request);
        if (req == null) {
            return;
        }
        int consumeMs = (int) (System.currentTimeMillis() - req.time);
        statistics(String.join(".", request.getRemote(), request.getMethod()).intern(), consumeMs, req.size, respSize);
    }

    private void statistics(String remoteMethod, int consumeMs, int reqSize, int respSize) {
        Statistics statistics = remoteMethodMap.computeIfAbsent(remoteMethod, (key) -> new Statistics());
        statistics.consumeMs.add(consumeMs);
        statistics.reqSize.add(reqSize);
        statistics.respSize.add(respSize);
    }

    private void exportToFile() {
        if (remoteMethodMap.isEmpty()) {
            return;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        File file = new File(exportDir);
        file.mkdirs();
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(new File(file, String.format("remote_statistics_%s.csv", simpleDateFormat.format(new Date())))));
            out.write("Remote,Called,AvgMs,MinMs,MaxMs,AvgReqSize,MinReqSize,MaxReqSize,AvgRespSize,MinRespSize,MaxRespSize");
            for (Map.Entry<String, Statistics> entry : remoteMethodMap.entrySet()) {
                out.newLine();
                Statistics statistics = entry.getValue();
                out.write(String.join(",",
                        entry.getKey(),
                        String.valueOf(statistics.consumeMs.count),
                        String.valueOf(statistics.consumeMs.avg()),
                        String.valueOf(statistics.consumeMs.min),
                        String.valueOf(statistics.consumeMs.max),
                        String.valueOf(statistics.reqSize.avg()),
                        String.valueOf(statistics.reqSize.min),
                        String.valueOf(statistics.reqSize.max),
                        String.valueOf(statistics.respSize.avg()),
                        String.valueOf(statistics.respSize.min),
                        String.valueOf(statistics.respSize.max)));
            }
            out.flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private static RemoteStatistics get() {
        return RemoteStatistics.SingleCase.INSTANCE;
    }

    public static void shutdown() {
        open = false;
    }

    public static void setPath(String path) {
        if (open) {
            get().setExportDir(path);
        }
    }

    public static boolean isOpen() {
        return open;
    }

    public static void start(SessionRequest request, int reqSize) {
        if (open) {
            get().add(request, reqSize);
        }
    }

    public static void finish(SessionRequest request, int respSize) {
        if (open) {
            get().finishRemove(request, respSize);
        }
    }

    public static void discard(SessionRequest request) {
        if (open) {
            get().remove(request);
        }
    }

    public static void print() {
        if (open) {
            get().exportToFile();
        }
    }

}
