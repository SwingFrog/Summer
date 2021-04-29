package com.swingfrog.summer.util;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class PollingUtil {

    public static <T> T getNext(AtomicInteger next, List<T> list, Predicate<T> priority) {
        if (list.isEmpty()) {
            return null;
        }
        List<T> temp = Lists.newArrayListWithCapacity(list.size());
        for (T t : list) {
            if (priority.test(t)) {
                temp.add(t);
            }
        }
        if (temp.isEmpty()) {
            temp = list;
        }
        int size = temp.size();
        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return temp.get(0);
        }
        int n = next.getAndIncrement();
        n = Math.abs(n);
        n = n % size;
        return temp.get(n);
    }

}
