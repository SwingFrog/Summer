package com.swingfrog.summer.web.view;

import com.swingfrog.summer.server.ServerContext;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.web.WebRequest;
import com.swingfrog.summer.web.view.render.DefaultWebViewRender;
import com.swingfrog.summer.web.view.render.WebViewRender;
import io.netty.buffer.ByteBuf;

public abstract class PlainView extends AbstractView {

    @Override
    public WebViewRender onRender(ServerContext serverContext, SessionContext sctx, WebRequest request) throws Exception {
        byte[] bytes = getText(serverContext, sctx, request).getBytes(serverContext.getConfig().getCharset());
        ByteBuf byteBuf = sctx.alloc().directBuffer(bytes.length);
        byteBuf.writeBytes(bytes);
        return new DefaultWebViewRender(byteBuf);
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    protected abstract String getText(ServerContext serverContext, SessionContext sctx, WebRequest request);

}
