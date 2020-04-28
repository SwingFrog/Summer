package com.swingfrog.summer.task;

import java.util.Date;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

public class TaskUtil {

	public static TaskTrigger getIntervalTask(long interval, long delay, Class<? extends Job> jobClass) {
		JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobClass.getName()).build();
		Trigger trigger;
		if (delay > 0) {
			trigger = TriggerBuilder.newTrigger()
					.withIdentity(jobClass.getName())
					.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(interval).repeatForever())
					.startAt(new Date(System.currentTimeMillis() + delay)).build();
		} else {
			trigger = TriggerBuilder.newTrigger()
					.withIdentity(jobClass.getName())
					.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(interval).repeatForever())
					.startNow().build();
		}
		return new TaskTrigger(job, trigger);
	}
	
	public static TaskTrigger getIntervalTask(long interval, long delay, String taskName, TaskJob taskJob) {
		JobDetail job = JobBuilder.newJob(TaskObjJob.class).withIdentity(taskName).build();
		Trigger trigger;
		if (delay > 0) {
			trigger = TriggerBuilder.newTrigger()
					.withIdentity(taskName)
					.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(interval).repeatForever())
					.startAt(new Date(System.currentTimeMillis() + delay)).build();
		} else {
			trigger = TriggerBuilder.newTrigger()
					.withIdentity(taskName)
					.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(interval).repeatForever())
					.startNow().build();
		}
		TaskObjJob.bindTriggerWithObj(trigger, taskJob);
		return new TaskTrigger(job, trigger);
	}
	
	public static TaskTrigger getCronTask(String cron, Class<? extends Job> jobClass) {
		JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobClass.getName()).build();
		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(jobClass.getName())
				.withSchedule(CronScheduleBuilder.cronSchedule(cron))
				.startNow().build();
		return new TaskTrigger(job, trigger);
	}
	
	public static TaskTrigger getCronTask(String cron, String taskName, TaskJob taskJob) {
		JobDetail job = JobBuilder.newJob(TaskObjJob.class).withIdentity(taskName).build();
		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(taskName)
				.withSchedule(CronScheduleBuilder.cronSchedule(cron))
				.startNow().build();
		TaskObjJob.bindTriggerWithObj(trigger, taskJob);
		return new TaskTrigger(job, trigger);
	}
	
	public static TaskTrigger getIntervalTask(long interval, long delay, MethodInvoke methodInvoke) {
		JobDetail job = JobBuilder.newJob(TaskMethodJob.class).withIdentity(methodInvoke.getMethod().getName(), methodInvoke.getObj().getClass().getName()).build();
		Trigger trigger;
		if (delay > 0) {
			trigger = TriggerBuilder.newTrigger()
					.withIdentity(methodInvoke.getMethod().getName(), methodInvoke.getObj().getClass().getName())
					.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(interval).repeatForever())
					.startAt(new Date(System.currentTimeMillis() + delay)).build();
		} else {
			trigger = TriggerBuilder.newTrigger()
					.withIdentity(methodInvoke.getMethod().getName(), methodInvoke.getObj().getClass().getName())
					.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(interval).repeatForever())
					.startNow().build();
		}
		TaskMethodJob.bindTriggerWithMethod(trigger, methodInvoke);
		return new TaskTrigger(job, trigger);
	}
	
	public static TaskTrigger getCronTask(String cron, MethodInvoke methodInvoke) {
		JobDetail job = JobBuilder.newJob(TaskMethodJob.class).withIdentity(methodInvoke.getMethod().getName(), methodInvoke.getObj().getClass().getName()).build();
		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(methodInvoke.getMethod().getName(), methodInvoke.getObj().getClass().getName())
				.withSchedule(CronScheduleBuilder.cronSchedule(cron))
				.startNow().build();
		TaskMethodJob.bindTriggerWithMethod(trigger, methodInvoke);
		return new TaskTrigger(job, trigger);
	}
}
