package com.swingfrog.summer.task;

import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskMethodJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(TaskMethodJob.class);
	private static final ConcurrentMap<Trigger, MethodInvoke> triggerMap = Maps.newConcurrentMap();
	
	public static void bindTriggerWithMethod(Trigger trigger, MethodInvoke methodInvoke) {
		triggerMap.put(trigger, methodInvoke);
	}
	
	@Override
	public void execute(JobExecutionContext context) {
		MethodInvoke methodInvoke = triggerMap.get(context.getTrigger());
		if (methodInvoke != null) {
			try {
				methodInvoke.invoke();
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
			}
		}
	}

}
