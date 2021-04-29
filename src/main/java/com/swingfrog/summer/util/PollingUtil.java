package com.swingfrog.summer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class PollingUtil {

    public static <T> T getNext(AtomicInteger next, List<T> list, Predicate<T> priority) {
        int listSize = list.size();
        if (listSize == 0) {
            return null;
        }
        if (listSize == 1) {
            return list.get(0);
        }
        List<T> temp = new ArrayList<>(list.size());
        for (T t : list) {
            if (priority.test(t)) {
                temp.add(t);
            }
        }
        if (temp.isEmpty()) {
            temp = list;
        }
        int tempSize = temp.size();
        if (tempSize == 0) {
            return null;
        }
        if (tempSize == 1) {
            return temp.get(0);
        }
        int n = next.getAndIncrement();
        n = Math.abs(n);
        n = n % tempSize;
        return temp.get(n);
    }

}
