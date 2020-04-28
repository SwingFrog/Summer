package com.swingfrog.summer.task;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskMgr {

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
		SchedulerFactory schedulerFactory = new StdSchedulerFactory(fileName);
		scheduler = schedulerFactory.getScheduler();
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
