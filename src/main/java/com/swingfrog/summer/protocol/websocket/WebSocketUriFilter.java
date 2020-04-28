package com.swingfrog.summer.protocol.websocket;

import com.swingfrog.summer.server.ServerContext;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.server.exception.WebSocketUriNoFoundException;

import com.swingfrog.summer.util.ForwardedAddressUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

public class WebSocketUriFilter extends SimpleChannelInboundHandler<FullHttpRequest> {

	private final String wsUri;
	private final ServerContext serverContext;
	 
    public WebSocketUriFilter(ServerContext serverContext) {
        this.wsUri = "/" + serverContext.getConfig().getServerName();
		this.serverContext = serverContext;
    }
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
		if (wsUri.equalsIgnoreCase(request.uri())) {
			SessionContext sctx = serverContext.getSessionContextGroup().getSessionByChannel(ctx);
			if (sctx != null)
				sctx.setRealAddress(ForwardedAddressUtil.parse(request.headers().get(ForwardedAddressUtil.KEY)));
            ctx.fireChannelRead(request.retain());
        } else {
        	ctx.close();
        	throw new WebSocketUriNoFoundException(request.uri());
        }
	}

}
