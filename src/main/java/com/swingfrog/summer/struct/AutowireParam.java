package com.swingfrog.summer.struct;

import com.google.common.collect.Maps;

import java.util.Map;

public class AutowireParam {

    private final Map<Class<?>, Object> types = Maps.newHashMap();
    private final Map<String, Object> names = Maps.newHashMap();

    public Map<Class<?>, Object> getTypes() {
        return types;
    }

    public Map<String, Object> getNames() {
        return names;
    }

}
