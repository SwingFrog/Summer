package com.swingfrog.summer.task;

import com.swingfrog.summer.task.cron.CronSequenceGenerator;

public class TaskUtil {
	
	public static TaskTrigger getIntervalTask(long interval, long delay, boolean nextMinuteBegin, Runnable runnable) {
		return TaskTrigger.ofInterval(interval, delay, nextMinuteBegin, runnable);
	}
	
	public static TaskTrigger getCronTask(String cron, Runnable runnable) {
		return TaskTrigger.ofCron(new CronSequenceGenerator(cron), runnable);
	}
	
	public static TaskTrigger getIntervalTask(long interval, long delay, boolean nextMinuteBegin, MethodInvoke methodInvoke) {
		return TaskTrigger.ofInterval(interval, delay, nextMinuteBegin, methodInvoke);
	}
	
	public static TaskTrigger getCronTask(String cron, MethodInvoke methodInvoke) {
		return TaskTrigger.ofCron(new CronSequenceGenerator(cron), methodInvoke);
	}
}
