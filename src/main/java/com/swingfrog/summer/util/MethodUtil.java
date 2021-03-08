package com.swingfrog.summer.util;

import com.google.common.collect.Sets;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

public class MethodUtil {

    public static boolean contains(Class<?> clazz, Method method) {
        int parameterCount = method.getParameterCount();
        String name = method.getName();
        Set<Class<?>> parameterTypes = Sets.newHashSet(method.getParameterTypes());
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if (declaredMethod.getParameterCount() != parameterCount)
                continue;
            if (!declaredMethod.getName().equals(name))
                continue;
            if (parameterTypes.containsAll(Arrays.asList(declaredMethod.getParameterTypes())))
                return true;
        }
        return false;
    }

}
