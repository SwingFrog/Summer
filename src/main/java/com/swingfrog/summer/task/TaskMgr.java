package com.swingfrog.summer.task;

import com.swingfrog.summer.config.ConfigUtil;
import com.swingfrog.summer.util.ThreadCountUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.io.*;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class TaskMgr {

	private static final Logger log = LoggerFactory.getLogger(TaskMgr.class);

	public static final String DEFAULT_CONFIG_PATH = "config/task.properties";

	private volatile ScheduledExecutorService scheduledExecutor;
	private final TaskConfig config = new TaskConfig();
	
	private static class SingleCase {
		public static final TaskMgr INSTANCE = new TaskMgr();
	}
	
	private TaskMgr() {

	}
	
	public static TaskMgr get() {
		return SingleCase.INSTANCE;
	}
	
	public void init(String path) throws IOException, IntrospectionException {
		log.info("task init");
		if (DEFAULT_CONFIG_PATH.equals(path)) {
			File file = new File(path);
			if (file.exists()) {
				loadConfig(new FileInputStream(file));
			} else {
				loadDefaultConfig();
			}
		} else {
			loadConfig(new FileInputStream(path));
		}
		log.info("task manager loading config, core thread num[{}]", config.getCoreThread());
	}

	private void loadConfig(InputStream in) throws IOException, IntrospectionException {
		Properties pro = new Properties();
		pro.load(in);
		ConfigUtil.loadDataWithBean(pro, "", config);
		in.close();
		pro.clear();
		config.setCoreThread(ThreadCountUtil.ioDenseness(config.getCoreThread()));
	}

	private void loadDefaultConfig() {
		config.setCoreThread(1);
	}

	public ScheduledExecutorService getScheduledExecutor() {
		if (scheduledExecutor == null) {
			synchronized (this) {
				if (scheduledExecutor == null) {
					scheduledExecutor = Executors.newScheduledThreadPool(
							config.getCoreThread(),
							new DefaultThreadFactory("TaskMgr"));
					log.info("task manager create scheduled executor");
				}
			}
		}
		return scheduledExecutor;
	}
	
	public void shutdown() {
		log.info("task shutdown all");
		if (scheduledExecutor != null) {
			scheduledExecutor.shutdown();
		}
	}
	
	public void start(TaskTrigger taskTrigger) {
		taskTrigger.start(getScheduledExecutor());
	}
	
	public void stop(TaskTrigger taskTrigger) {
		taskTrigger.stop(false);
	}
	
}
