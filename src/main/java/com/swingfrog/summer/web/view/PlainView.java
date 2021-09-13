package com.swingfrog.summer.web.view;

import com.swingfrog.summer.server.ServerContext;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.util.ChunkedByteBuf;
import com.swingfrog.summer.web.WebRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.stream.ChunkedInput;

public abstract class PlainView extends AbstractView {

    @Override
    public ChunkedInput<ByteBuf> onRender(ServerContext serverContext, SessionContext sctx, WebRequest request) throws Exception {
        byte[] bytes = getText(serverContext, sctx, request).getBytes(serverContext.getConfig().getCharset());
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        return new ChunkedByteBuf(byteBuf);
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    protected abstract String getText(ServerContext serverContext, SessionContext sctx, WebRequest request);

}
