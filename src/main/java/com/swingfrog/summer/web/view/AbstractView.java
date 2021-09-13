package com.swingfrog.summer.web.view;

import com.google.common.collect.Maps;
import java.util.Map;

public abstract class AbstractView implements WebView {

    protected volatile Map<String, String> headers;

    @Override
    public int getStatus() {
        return 200;
    }

    public void addHeader(String key, String value) {
        if (headers == null) {
            synchronized (this) {
                if (headers == null) {
                    headers = Maps.newConcurrentMap();
                }
            }
        }
        headers.put(key, value);
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

}
