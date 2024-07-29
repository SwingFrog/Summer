package com.swingfrog.summer.client;

import com.swingfrog.summer.protocol.ProtocolConst;
import com.swingfrog.summer.task.TaskTrigger;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swingfrog.summer.config.ClientConfig;
import com.swingfrog.summer.task.TaskMgr;
import com.swingfrog.summer.task.TaskUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.TimeUnit;

public class Client {

	private static final Logger log = LoggerFactory.getLogger(Client.class);
	private final ClientContext clientContext;
	private final ClientRemote clientRemote;
	private final EventLoopGroup workerGroup;
	private final TaskTrigger checkHeartTask;
	private volatile boolean active;
	
	public Client(int id, ClientConfig config, EventLoopGroup workerGroup) {
		log.info("client cluster {}", config.getCluster());
		log.info("client serverName {}", config.getServerName());
		log.info("client address {}", config.getAddress());
		log.info("client port {}", config.getPort());
		log.info("client protocol {}", config.getProtocol());
		log.info("client charset {}", config.getCharset());
		log.info("client password {}", config.getPassword());
		log.info("client msgLength {}", config.getMsgLength());
		log.info("client heartSec {}", config.getHeartSec());
		log.info("client reconnectMs {}", config.getReconnectMs());
		log.info("client syncRemoteTimeOutMs {}", config.getSyncRemoteTimeOutMs());
		log.info("client connectNum {}", config.getConnectNum());
		this.workerGroup = workerGroup;
		clientContext = new ClientContext(id, config, this);
		clientRemote = new DefaultClientRemote(clientContext);

		long intervalTime = clientContext.getConfig().getHeartSec() * 1000L;
		checkHeartTask = TaskUtil.getIntervalTask(intervalTime / 2, intervalTime / 2, false, () -> {
			if (clientContext.getChannel() != null) {
				log.info("check connect for {}_{}", clientContext.getConfig().getServerName(), id);
				clientContext.getChannel().writeAndFlush(ProtocolConst.PING);
				long time = System.currentTimeMillis() - intervalTime;
				long recvTime = clientContext.getLastRecvTime();
				if (recvTime < time) {
					clientContext.getChannel().close();
				}
			}
		});
	}

	public void connect() {
		active = true;
		TaskMgr.get().start(checkHeartTask);
		reconnect();
	}

	public void reconnect() {
		if (!active) {
			return;
		}
		try {
			log.info("client[{}] connect {}:{}", clientContext.getConfig().getServerName(), clientContext.getConfig().getAddress(), clientContext.getConfig().getPort());
			Bootstrap b = new Bootstrap();
			b.group(workerGroup)
					.channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new ClientInitializer(clientContext));
			b.remoteAddress(clientContext.getConfig().getAddress(), clientContext.getConfig().getPort());
			b.connect().addListener(new ConnectionListener());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void shutdown() {
		log.info("client[{}] shutdown", clientContext.getConfig().getServerName());
		active = false;
		try {
			TaskMgr.get().stop(checkHeartTask);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private class ConnectionListener implements ChannelFutureListener {
		@Override
		public void operationComplete(ChannelFuture f) {
			if (f.isSuccess()) {
				log.info("connect {} success", clientContext.getConfig().getServerName());
			} else {
				if (active) {
					ClientReconnectExeMgr.get().getExecutor().schedule(() -> {
						log.info("reconnect for {}", clientContext.getConfig().getServerName());
						reconnect();
					}, clientContext.getConfig().getReconnectMs(), TimeUnit.MILLISECONDS);
				}
			}
		} 
	}
	
	public ClientRemote getClientRemote() {
		return clientRemote;
	}

	public boolean isActive() {
		return active;
	}

	public ClientContext getClientContext() {
		return clientContext;
	}

}
