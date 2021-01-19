package com.swingfrog.summer.server;

import com.google.common.collect.Sets;
import com.swingfrog.summer.protocol.ProtocolConst;
import com.swingfrog.summer.server.async.ProcessResult;
import com.swingfrog.summer.server.rpc.RpcClientConst;
import com.swingfrog.summer.statistics.RemoteStatistics;
import com.swingfrog.summer.struct.AutowireParam;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.protocol.SessionResponse;
import com.swingfrog.summer.server.exception.CodeException;
import com.swingfrog.summer.server.exception.SessionException;
import com.swingfrog.summer.server.rpc.RpcClientMgr;

import java.util.Set;

public class ServerStringHandler extends AbstractServerHandler<String> {
	
	private static final Logger log = LoggerFactory.getLogger(ServerStringHandler.class);

	public ServerStringHandler(ServerContext serverContext) {
		super(serverContext);
	}
	
	@Override
	protected void recv(Channel channel, SessionContext sctx, String msg) {
		if (ProtocolConst.PING.equals(msg)) {
			channel.writeAndFlush(ProtocolConst.PONG);
			return;
		} else if (msg.startsWith(ProtocolConst.RPC)) {
			String[] text = msg.split(ProtocolConst.RPC_SPLIT);
			RpcClientMgr.get().add(sctx, text[1], text[2]);
			Set<Long> requestResult = Sets.newConcurrentHashSet();
			sctx.put(RpcClientConst.SESSION_KEY_REQUEST_RESULT, requestResult);
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
				if (!channel.isActive()) {
					RemoteStatistics.discard(sctx, request);
					return;
				}
				try {
					if (sctx.containsKey(RpcClientConst.SESSION_KEY_REQUEST_RESULT)) {
						Set<Long> requestResult = sctx.get(RpcClientConst.SESSION_KEY_REQUEST_RESULT);
						if (!requestResult.add(request.getId())) {
							String response = SessionResponse.buildError(request, SessionException.REPEATED_REQUEST).toJSONString();
							log.debug("server response error {} to {}", response, sctx);
							writeResponse(sctx, response);
							RemoteStatistics.finish(sctx, request, response.length());
							return;
						}
					}

					ProcessResult<SessionResponse> processResult = RemoteDispatchMgr.get().process(serverContext, request, sctx, autowireParam);
					if (processResult.isAsync()) {
						return;
					}
					String response = processResult.getValue().toJSONString();
					log.debug("server response {} to {}", response, sctx);
					writeResponse(sctx, response);
					RemoteStatistics.finish(sctx, request, response.length());
				} catch (CodeException ce) {
					log.warn(ce.getMessage(), ce);
					String response = SessionResponse.buildError(request, ce).toJSONString();
					log.debug("server response error {} to {}", response, sctx);
					writeResponse(sctx, response);
					RemoteStatistics.finish(sctx, request, response.length());
				} catch (Throwable e) {
					log.error(e.getMessage(), e);
					String response = SessionResponse.buildError(request, SessionException.INVOKE_ERROR).toJSONString();
					log.debug("server response error {} to {}", response, sctx);
					writeResponse(sctx, response);
					RemoteStatistics.finish(sctx, request, response.length());
				}
			};
			submitRunnable(sctx, request, runnable);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			serverContext.getSessionHandlerGroup().unableParseMsg(sctx);
		}
	}

	private void writeResponse(SessionContext sctx, String response) {
		ServerWriteHelper.write(serverContext, sctx, response);
	}

}
