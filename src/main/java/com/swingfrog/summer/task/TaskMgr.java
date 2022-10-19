package com.swingfrog.summer.task;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

public class TaskMgr {

	public static final String DEFAULT_CONFIG_PATH = "config/task.properties";

	private static final Logger log = LoggerFactory.getLogger(TaskMgr.class);
	private Scheduler scheduler;
	
	private static class SingleCase {
		public static final TaskMgr INSTANCE = new TaskMgr();
	}
	
	private TaskMgr() {

	}
	
	public static TaskMgr get() {
		return SingleCase.INSTANCE;
	}
	
	public void init(String fileName) throws SchedulerException {
		log.info("task init");
		SchedulerFactory schedulerFactory;
		if (DEFAULT_CONFIG_PATH.equals(fileName)) {
			if (new File(fileName).exists()) {
				schedulerFactory = new StdSchedulerFactory(fileName);
			} else {
				log.debug("used default task config.");
				schedulerFactory = new StdSchedulerFactory(createDefaultProperties());
			}
		} else {
			schedulerFactory = new StdSchedulerFactory(fileName);
		}
		scheduler = schedulerFactory.getScheduler();
	}

	private Properties createDefaultProperties() {
		Properties properties = new Properties();
		properties.setProperty("org.quartz.scheduler.instanceName", "Task");
		properties.setProperty("org.quartz.scheduler.rmi.export", "false");
		properties.setProperty("org.quartz.scheduler.rmi.proxy", "false");
		properties.setProperty("org.quartz.scheduler.wrapJobExecutionInUserTransaction", "false");
		properties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
		properties.setProperty("org.quartz.threadPool.threadCount", "1");
		properties.setProperty("org.quartz.threadPool.threadPriority", "5");
		properties.setProperty("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", "true");
		properties.setProperty("org.quartz.jobStore.misfireThreshold", "60000");
		properties.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
		return properties;
	}
	
	public void startAll() throws SchedulerException {
		log.info("task start all");
		scheduler.start();
	}
	
	
	public void shutdownAll() throws SchedulerException {
		log.info("task shutdown all");
		scheduler.shutdown();
	}
	
	public void start(TaskTrigger taskTrigger) throws SchedulerException {
		scheduler.scheduleJob(taskTrigger.getJob(), taskTrigger.getTrigger());
	}
	
	public void stop(TaskTrigger taskTrigger) throws SchedulerException {
		scheduler.unscheduleJob(taskTrigger.getTrigger().getKey());
	}
	
}
