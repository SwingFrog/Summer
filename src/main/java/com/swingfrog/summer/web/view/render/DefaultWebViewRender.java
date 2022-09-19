package com.swingfrog.summer.web.view.render;

import io.netty.buffer.ByteBuf;
import io.netty.channel.FileRegion;
import io.netty.handler.stream.ChunkedInput;

public class DefaultWebViewRender implements WebViewRender {

    private final Object data;
    private final long size;

    public DefaultWebViewRender(ByteBuf byteBuf) {
        data = byteBuf;
        size = byteBuf.readableBytes();
    }

    public DefaultWebViewRender(ChunkedInput<ByteBuf> chunkedInput) {
        data = chunkedInput;
        size = chunkedInput.length();
    }

    public DefaultWebViewRender(FileRegion fileRegion) {
        data = fileRegion;
        size = fileRegion.count();
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public long getSize() {
        return size;
    }

}
