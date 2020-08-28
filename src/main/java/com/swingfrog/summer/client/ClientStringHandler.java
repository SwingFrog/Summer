package com.swingfrog.summer.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.swingfrog.summer.config.ConfigMgr;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.protocol.SessionResponse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientStringHandler extends SimpleChannelInboundHandler<String> {

	private static final Logger log = LoggerFactory.getLogger(ClientStringHandler.class);
	private final ClientContext clientContext;
	
	public ClientStringHandler(ClientContext clientContext) {
		this.clientContext = clientContext;
	}
	
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) {

	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		clientContext.setChannel(ctx);
		ctx.writeAndFlush(String.format("rpc\t%s\t%s", ConfigMgr.get().getServerConfig().getCluster(), ConfigMgr.get().getServerConfig().getServerName()));
		SessionRequest sessionRequest;
		while ((sessionRequest = clientContext.getRequestQueue().poll()) != null) {
			sessionRequest.setId(ClientMgr.get().incrementCurrentId());
			ctx.writeAndFlush(sessionRequest.toJSONString());
		}
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		log.warn("client connect break");
		clientContext.setChannel(null);
		ctx.channel().eventLoop().execute(() -> {
			try {
				Thread.sleep(clientContext.getConfig().getReconnectMs());
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
			clientContext.getClient().connect();
		});
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) {
		clientContext.setHeartCount(0);
		if ("pong".equals(msg)) {
			
		} else {
			try {
				SessionResponse response = JSON.parseObject(msg, SessionResponse.class);
				if (response.getId() == 0) {
					clientContext.getPushGroup().execute(()->{
						PushDispatchMgr.get().processPush(response);
					});
				} else {
					clientContext.getEventGroup().execute(()->{
						PushDispatchMgr.get().processRemote(response);
					});
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error(cause.getMessage(), cause);
	}

}
