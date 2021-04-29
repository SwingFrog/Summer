package com.swingfrog.summer.task;

import com.google.common.collect.Maps;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;

import java.util.concurrent.ConcurrentMap;

public class TaskObjJob implements Job {

	private static final ConcurrentMap<Trigger, TaskJob> triggerMap = Maps.newConcurrentMap();
	
	public static void bindTriggerWithObj(Trigger trigger, TaskJob taskJob) {
		triggerMap.put(trigger, taskJob);
	}
	
	@Override
	public void execute(JobExecutionContext context) {
		TaskJob taskJob = triggerMap.get(context.getTrigger());
		if (taskJob != null) {
			taskJob.execute();
		}
	}
}
