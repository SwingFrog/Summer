package com.swingfrog.summer.client;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swingfrog.summer.config.ClientConfig;
import com.swingfrog.summer.task.TaskMgr;
import com.swingfrog.summer.task.TaskUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

public class Client {

	private static final Logger log = LoggerFactory.getLogger(Client.class);
	private final ClientContext clientContext;
	private final ClientRemote clientRemote;
	private final EventLoopGroup workerGroup;
	private final int id;
	
	public Client(int id, ClientConfig config) throws SchedulerException {
		this.id = id;
		log.info("client cluster {}", config.getCluster());
		log.info("client serverName {}", config.getServerName());
		log.info("client address {}", config.getAddress());
		log.info("client port {}", config.getPort());
		log.info("client protocol {}", config.getProtocol());
		log.info("client charset {}", config.getCharset());
		log.info("client password {}", config.getPassword());
		log.info("client workerThread {}", config.getWorkerThread());
		log.info("client eventThread {}", config.getEventThread());
		log.info("client msgLength {}", config.getMsgLength());
		log.info("client heartSec {}", config.getHeartSec());
		log.info("client reconnectMs {}", config.getReconnectMs());
		log.info("client syncRemoteTimeOutMs {}", config.getSyncRemoteTimeOutMs());
		log.info("client connectNum {}", config.getConnectNum());
		workerGroup = new NioEventLoopGroup(config.getWorkerThread(), new DefaultThreadFactory("ClientWorker", true));
		clientContext = new ClientContext(config, this, new NioEventLoopGroup(config.getEventThread(), new DefaultThreadFactory("ClientEvent", true)));
		clientRemote = new ClientRemote(clientContext);
		startCheckHeartTimeTask();
	}

	public void connect() {
		try {
			log.info("client[{}] connect {}:{}", clientContext.getConfig().getServerName(), clientContext.getConfig().getAddress(), clientContext.getConfig().getPort());
			Bootstrap b = new Bootstrap();
			b.group(workerGroup).channel(NioSocketChannel.class).handler(new ClientInitializer(clientContext));
			b.remoteAddress(clientContext.getConfig().getAddress(), clientContext.getConfig().getPort());
			b.connect().addListener(new ConnectionListener());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void shutdown() {
		log.info("client[{}] shutdown", clientContext.getConfig().getServerName());
		try {
			workerGroup.shutdownGracefully().sync();
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	private class ConnectionListener implements ChannelFutureListener {
		@Override
		public void operationComplete(ChannelFuture f) {
			if (f.isSuccess()) {
				log.info("connect {} success", clientContext.getConfig().getServerName());
			} else {
				f.channel().eventLoop().execute(()->{
					try {
						Thread.sleep(clientContext.getConfig().getReconnectMs());
					} catch (InterruptedException e) {
						log.error(e.getMessage(), e);
					}
					log.info("reconnect for {}", clientContext.getConfig().getServerName());
					connect();
				});
			}
		} 
	}
	
	public void destroyWorkerGroup() {
		workerGroup.shutdownGracefully();
	}
	
	public ClientRemote getClientRemote() {
		return clientRemote;
	}

	private void startCheckHeartTimeTask() throws SchedulerException {
		int interval = clientContext.getConfig().getHeartSec() / 2;
		TaskMgr.get().start(TaskUtil.getIntervalTask(interval * 1000, interval * 1000, clientContext.getConfig().getServerName()+"_"+id, () -> {
			if (clientContext.getChannel() != null) {
				log.info("check connect for {}_{}", clientContext.getConfig().getServerName(), id);
				clientContext.getChannel().writeAndFlush("ping");
				clientContext.setHeartCount(clientContext.getHeartCount() + interval);
				if (clientContext.getHeartCount() > clientContext.getConfig().getHeartSec()) {
					clientContext.getChannel().close();
				}
			}
		}));
	}
}
