package com.swingfrog.summer.server;

import com.swingfrog.summer.protocol.ProtocolConst;
import com.swingfrog.summer.server.async.ProcessResult;
import com.swingfrog.summer.statistics.RemoteStatistics;
import com.swingfrog.summer.struct.AutowireParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.protocol.SessionResponse;
import com.swingfrog.summer.server.exception.CodeException;
import com.swingfrog.summer.server.exception.SessionException;
import com.swingfrog.summer.server.rpc.RpcClientMgr;

import io.netty.channel.ChannelHandlerContext;

public class ServerStringHandler extends AbstractServerHandler<String> {
	
	private static final Logger log = LoggerFactory.getLogger(ServerStringHandler.class);

	public ServerStringHandler(ServerContext serverContext) {
		super(serverContext);
	}
	
	@Override
	protected void recv(ChannelHandlerContext ctx, SessionContext sctx, String msg) {
		if (ProtocolConst.PING.equals(msg)) {
			ctx.writeAndFlush(ProtocolConst.PONG);
			return;
		} else if (msg.startsWith(ProtocolConst.RPC)) {
			String[] msgs = msg.split(ProtocolConst.RPC_SPLIT);
			RpcClientMgr.get().add(sctx, msgs[1], msgs[2]);
			return;
		}
		try {
			SessionRequest request = JSON.parseObject(msg, SessionRequest.class);
			if (request.getId() == sctx.getCurrentMsgId()) {
				serverContext.getSessionHandlerGroup().repetitionMsg(sctx);
				return;
			}
			sctx.setCurrentMsgId(request.getId());
			log.debug("server request {} from {}", msg, sctx);
			if (!serverContext.getSessionHandlerGroup().receive(sctx, request)) {
				return;
			}

			AutowireParam autowireParam = new AutowireParam();
			serverContext.getSessionHandlerGroup().autowireParam(sctx, autowireParam);

			RemoteStatistics.start(sctx, request, msg.length());
			Runnable runnable = () -> {
				if (!ctx.channel().isActive()) {
					RemoteStatistics.discard(sctx, request);
					return;
				}
				try {
					ProcessResult<SessionResponse> processResult = RemoteDispatchMgr.get().process(serverContext, request, sctx, autowireParam);
					if (processResult.isAsync()) {
						return;
					}
					String response = processResult.getValue().toJSONString();
					log.debug("server response {} to {}", response, sctx);
					writeResponse(ctx, sctx, response);
					RemoteStatistics.finish(sctx, request, response.length());
				} catch (CodeException ce) {
					log.warn(ce.getMessage(), ce);
					String response = SessionResponse.buildError(request, ce).toJSONString();
					log.debug("server response error {} to {}", response, sctx);
					writeResponse(ctx, sctx, response);
					RemoteStatistics.finish(sctx, request, response.length());
				} catch (Throwable e) {
					log.error(e.getMessage(), e);
					String response = SessionResponse.buildError(request, SessionException.INVOKE_ERROR).toJSONString();
					log.debug("server response error {} to {}", response, sctx);
					writeResponse(ctx, sctx, response);
					RemoteStatistics.finish(sctx, request, response.length());
				}
			};
			submitRunnable(sctx, request, runnable);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			serverContext.getSessionHandlerGroup().unableParseMsg(sctx);
		}
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		super.channelWritabilityChanged(ctx);
		SessionContext sctx = serverContext.getSessionContextGroup().getSessionByChannel(ctx);
		while (ctx.channel().isActive() && ctx.channel().isWritable() && sctx.getWaitWriteQueueSize() > 0) {
			ctx.writeAndFlush(sctx.getWaitWriteQueue().poll());
		}
	}

	private void writeResponse(ChannelHandlerContext ctx, SessionContext sctx, String response) {
		ServerWriteHelper.write(ctx, serverContext, sctx, response);
	}

}
