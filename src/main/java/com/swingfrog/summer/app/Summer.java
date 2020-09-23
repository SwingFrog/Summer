package com.swingfrog.summer.app;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.protobuf.Message;
import com.swingfrog.summer.db.repository.AsyncCacheRepositoryMgr;
import com.swingfrog.summer.db.repository.RepositoryMgr;
import com.swingfrog.summer.lifecycle.Lifecycle;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.protocol.protobuf.ProtobufRequest;
import com.swingfrog.summer.server.async.AsyncResponseMgr;
import com.swingfrog.summer.statistics.RemoteStatistics;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swingfrog.summer.client.ClientMgr;
import com.swingfrog.summer.client.ClientRemote;
import com.swingfrog.summer.concurrent.SessionQueueMgr;
import com.swingfrog.summer.concurrent.SingleQueueMgr;
import com.swingfrog.summer.concurrent.SynchronizedMgr;
import com.swingfrog.summer.config.ConfigMgr;
import com.swingfrog.summer.db.DataBaseMgr;
import com.swingfrog.summer.event.EventBusMgr;
import com.swingfrog.summer.ioc.ContainerMgr;
import com.swingfrog.summer.loader.JarLoader;
import com.swingfrog.summer.proxy.ProxyFactory;
import com.swingfrog.summer.proxy.ProxyMethodInterceptor;
import com.swingfrog.summer.redis.RedisMgr;
import com.swingfrog.summer.redis.RedisSource;
import com.swingfrog.summer.server.ServerMgr;
import com.swingfrog.summer.server.ServerPush;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.server.exception.CodeException;
import com.swingfrog.summer.server.exception.CodeMsg;
import com.swingfrog.summer.task.TaskJob;
import com.swingfrog.summer.task.TaskMgr;
import com.swingfrog.summer.task.TaskTrigger;
import com.swingfrog.summer.task.TaskUtil;
import com.swingfrog.summer.web.WebMgr;

public class Summer {
	
	private static final Logger log = LoggerFactory.getLogger(Summer.class);
	public static final String NAME = "Summer";
	
	public static void main(String[] args) {
		try {
			if (args.length == 0) {
				System.out.println("hello, you need to fill in two parameters to start the application.");
				System.out.println();
				System.out.println("args:\n[app jar] [main class]");
				System.out.println();
				System.out.println("example:\nxxx.jar xxx.xxx.xxx.xxx");
				return;
			}
			String appJar = args[0];
			String mainClass = args[1];
			JarLoader.loadJar(appJar);
			Class<?> clazz = Class.forName(mainClass);
			Method method = clazz.getMethod("main", String[].class);
			method.invoke(clazz, new Object[]{args});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			System.exit(-1);
		}
	}
	
	public static void hot(SummerApp app) {
		hot(app, app.getClass().getPackage().getName());
	}
	
	public static void hot(SummerApp app, String projectPackage) {
		hot(SummerConfig.newBuilder()
				.app(app)
				.projectPackage(projectPackage)
				.build());
	}

	public static void hot(SummerConfig config) {
		hot(config.getApp(),
				config.getProjectPackage() == null ? config.getApp().getClass().getPackage().getName() : config.getProjectPackage(),
				config.getLibPath() == null ? "lib" : config.getLibPath(),
				config.getServerProperties() == null ? "config/server.properties" : config.getServerProperties(),
				config.getRedisProperties() == null ? "config/redis.properties" : config.getRedisProperties(),
				config.getDbProperties() == null ? "config/db.properties" : config.getDbProperties(),
				config.getTaskProperties() == null ? "config/task.properties" : config.getTaskProperties(),
				config.getSessionQueueExpireTimeMs() == null ? TimeUnit.MINUTES.toMillis(10) : config.getSessionQueueExpireTimeMs(),
				config.getSingleQueueExpireTimeMs() == null ? TimeUnit.MINUTES.toMillis(10) : config.getSingleQueueExpireTimeMs()
		);
	}

	public static void hot(SummerApp app, String projectPackage, String libPath,
			String serverProperties, String redisProperties, String dbProperties, String taskProperties,
						   long sessionQueueExpireTimeMs, long singleQueueExpireTimeMs) {
		try {
			logo();
			log.info("summer init...");
			JarLoader.loadJarByDir(new File(libPath));
			log.info("config load...");
			ConfigMgr.get().loadConfig(serverProperties);
			RedisMgr.get().loadConfig(redisProperties);
			DataBaseMgr.get().loadConfig(dbProperties);
			AsyncCacheRepositoryMgr.get().loadConfig(dbProperties);
			TaskMgr.get().init(taskProperties);
			ContainerMgr.get().init(projectPackage);
			ServerMgr.get().init();
			ClientMgr.get().init();
			EventBusMgr.get().init();
			SessionQueueMgr.get().init(ServerMgr.get().getEventExecutor(), sessionQueueExpireTimeMs);
			SingleQueueMgr.get().init(ServerMgr.get().getEventExecutor(), singleQueueExpireTimeMs);
			ContainerMgr.get().autowired();
			ContainerMgr.get().proxyObj();
			app.init();
			RepositoryMgr.get().init();
			log.info("summer launch...");
			ContainerMgr.get().listDeclaredComponent(Lifecycle.class).stream()
					.filter(l -> l.getInfo() != null)
					.sorted(Comparator.comparingInt(l -> -l.getInfo().getPriority()))
					.forEach(l -> {
						log.info("lifecycle [{}] start", l.getInfo().getName());
						l.start();
					});
			ContainerMgr.get().startTask();
			ServerMgr.get().launch();
			ClientMgr.get().connectAll();
			TaskMgr.get().startAll();
			app.start();
			Runtime.getRuntime().addShutdownHook(new Thread(()-> {
				ClientMgr.get().shutdown();
				ServerMgr.get().shutdown();
				try {
					TaskMgr.get().shutdownAll();
				} catch (SchedulerException e) {
					log.error(e.getMessage(), e);
				}
				app.stop();
				ContainerMgr.get().listDeclaredComponent(Lifecycle.class).stream()
						.filter(l -> l.getInfo() != null)
						.sorted(Comparator.comparingInt(l -> l.getInfo().getPriority()))
						.forEach(l -> {
							log.info("lifecycle [{}] stop", l.getInfo().getName());
							l.stop();
						});
				EventBusMgr.get().shutdown();
				AsyncCacheRepositoryMgr.get().shutdown();
				RemoteStatistics.print();
			}, "shutdown"));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			log.error("summer cooled");
			System.exit(-1);
		}
	}
	
	public static void sync(String key, Runnable runnable) {
		SynchronizedMgr.get().sync(key, runnable);
	}
	
	public static void execute(Object key, Runnable runnable) {
		SingleQueueMgr.get().execute(key, runnable);
	}

	public static void execute(SessionContext sctx, Runnable runnable) {
		SessionQueueMgr.get().execute(sctx, runnable);
	}
	
	public static void addComponent(Object obj) {
		ContainerMgr.get().addComponent(obj);
	}
	
	public static void removeComponent(Object obj) {
		ContainerMgr.get().removeComponent(obj);
	}
	
	public static <T> T getComponent(Class<?> clazz) {
		return ContainerMgr.get().getComponent(clazz);
	}
	
	public static <T> T getDeclaredComponent(Class<T> clazz) {
		return ContainerMgr.get().getDeclaredComponent(clazz);
	}
	
	public static <T> List<T> listDeclaredComponent(Class<T> clazz) {
		return ContainerMgr.get().listDeclaredComponent(clazz);
	}
	
	public static <T> T getProxyInstance(Object target, ProxyMethodInterceptor interceptor) {
		return ProxyFactory.getProxyInstance(target, interceptor);
	}
	
	public static void autowired(Object obj) {
		try {
			ContainerMgr.get().autowired(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public static RedisSource getRedisSource() {
		RedisSource rs = ContainerMgr.get().getComponent(RedisSource.class);
		if (rs == null) {
			rs = new RedisSource();
			ContainerMgr.get().addComponent(rs);
		}
		return rs;
	}
	
	public static TaskTrigger getIntervalTask(long interval, long delay, String taskName, TaskJob taskJob) {
		return TaskUtil.getIntervalTask(interval, delay, taskName, taskJob);
	}
	
	public static TaskTrigger getCronTask(String cron, String taskName, TaskJob taskJob) {
		return TaskUtil.getCronTask(cron, taskName, taskJob);
	}
	
	public static void startTask(TaskTrigger taskTrigger) {
		try {
			TaskMgr.get().start(taskTrigger);
		} catch (SchedulerException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public static void stopTask(TaskTrigger taskTrigger) {
		try {
			TaskMgr.get().stop(taskTrigger);
		} catch (SchedulerException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public static ClientRemote getClientRemote(String cluster, String name) {
		return ClientMgr.get().getClientRemote(cluster, name);
	}
	
	public static ClientRemote getRandomClientRemote(String cluster) {
		return ClientMgr.get().getRandomClientRemote(cluster);
	}
	
	public static <T> T getRemoteInvokeObject(String cluster, String name, Class<?> clazz) {
		return ClientMgr.get().getRemoteInvokeObject(cluster, name, clazz);
	}
	
	public static <T> T getRemoteInvokeObjectWithRetry(String cluster, String name, Class<?> clazz) {
		return ClientMgr.get().getRemoteInvokeObjectWithRetry(cluster, name, clazz);
	}
	
	public static <T> T getRandomRemoteInvokeObject(String cluster, Class<?> clazz) {
		return ClientMgr.get().getRandomRemoteInvokeObject(cluster, clazz);
	}
	
	public static <T> T getRandomRemoteInvokeObjectWithRetry(String cluster, Class<?> clazz) {
		return ClientMgr.get().getRandomRemoteInvokeObjectWithRetry(cluster, clazz);
	}
	
	public static ServerPush getServerPush() {
		return ServerMgr.get().getServerPush();
	}
	
	public static void closeSession(SessionContext sctx) {
		ServerMgr.get().closeSession(sctx);
	}
	
	public static ExecutorService getServerEventExecutor() {
		return ServerMgr.get().getEventExecutor();
	}
	
	public static int getSessionQueueSize(SessionContext sctx) {
		return SessionQueueMgr.get().getQueueSize(sctx);
	}
	
	public static CodeException createCodeException(long code, String msg) {
		return new CodeException(code, msg);
	}
	
	public static CodeException createCodeException(CodeMsg msg, Object ...args) {
		return new CodeException(msg, args);
	}
	
	public static CodeMsg createCodeMsg(long code, String msg) {
		return new CodeMsg(code, msg);
	}
	
	public static String getCluster() {
		return ConfigMgr.get().getServerConfig().getCluster();
	}
	
	public static String getServerName() {
		return ConfigMgr.get().getServerConfig().getServerName();
	}
	
	public static void syncDispatch(String eventName, Object ...args) {
		EventBusMgr.get().syncDispatch(eventName, args);
	}
	
	public static void asyncDispatch(String eventName, Object ...args) {
		EventBusMgr.get().asyncDispatch(eventName, args);
	}
	
	public static WebMgr getWeb() {
		return WebMgr.get();
	}

	public static void loadOtherDataSource(String topic, String dbProperties) {
		try {
			DataBaseMgr.get().loadConfigForOther(topic, dbProperties);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public static void asyncResponse(SessionContext sctx, SessionRequest request, Supplier<Object> runnable) {
		AsyncResponseMgr.get().process(sctx, request, runnable);
	}

	public static void asyncResponse(SessionContext sctx, SessionRequest request, Runnable runnable) {
		AsyncResponseMgr.get().process(sctx, request, runnable);
	}

	public static void asyncResponse(SessionContext sctx, ProtobufRequest request, Supplier<? extends Message> runnable) {
		AsyncResponseMgr.get().process(sctx, request, runnable);
	}
	
	public static void logo() {
		String logo = "\n" +
				"\n" +
				"   SSSSSSSSSSSSSSS\n SS:::::::::::::::S\nS:::::SSSSSS::::::S\nS:::::S     SSSSSSS\nS:::::S            uuuuuu    uuuuuu     mmmmmmm    mmmmmmm      mmmmmmm    mmmmmmm       eeeeeeeeeeee    rrrrr   rrrrrrrrr\nS:::::S            u::::u    u::::u   mm:::::::m  m:::::::mm  mm:::::::m  m:::::::mm   ee::::::::::::ee  r::::rrr:::::::::r\n S::::SSSS         u::::u    u::::u  m::::::::::mm::::::::::mm::::::::::mm::::::::::m e::::::eeeee:::::eer:::::::::::::::::r\n  SS::::::SSSSS    u::::u    u::::u  m::::::::::::::::::::::mm::::::::::::::::::::::me::::::e     e:::::err::::::rrrrr::::::r\n    SSS::::::::SS  u::::u    u::::u  m:::::mmm::::::mmm:::::mm:::::mmm::::::mmm:::::me:::::::eeeee::::::e r:::::r     r:::::r\n       SSSSSS::::S u::::u    u::::u  m::::m   m::::m   m::::mm::::m   m::::m   m::::me:::::::::::::::::e  r:::::r     rrrrrrr\n            S:::::Su::::u    u::::u  m::::m   m::::m   m::::mm::::m   m::::m   m::::me::::::eeeeeeeeeee   r:::::r\n            S:::::Su:::::uuuu:::::u  m::::m   m::::m   m::::mm::::m   m::::m   m::::me:::::::e            r:::::r\nSSSSSSS     S:::::Su:::::::::::::::uum::::m   m::::m   m::::mm::::m   m::::m   m::::me::::::::e           r:::::r\nS::::::SSSSSS:::::S u:::::::::::::::um::::m   m::::m   m::::mm::::m   m::::m   m::::m e::::::::eeeeeeee   r:::::r\nS:::::::::::::::SS   uu::::::::uu:::um::::m   m::::m   m::::mm::::m   m::::m   m::::m  ee:::::::::::::e   r:::::r\n SSSSSSSSSSSSSSS       uuuuuuuu  uuuummmmmm   mmmmmm   mmmmmmmmmmmm   mmmmmm   mmmmmm    eeeeeeeeeeeeee   rrrrrrr" +
				"\n" +
				"\n" +
				"\n" +
				"                                            Summer Server Powered by Toke 2018" +
				"\n" +
				"\n" +
				"-----------------------------------------------------------------------------------------------------------------------------";
		log.info(logo);
	}

}
