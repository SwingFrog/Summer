package com.swingfrog.summer.task;

import org.quartz.JobDetail;
import org.quartz.Trigger;

public class TaskTrigger {

	private JobDetail job;
	private Trigger trigger;
	
	public TaskTrigger(JobDetail job, Trigger trigger) {
		this.job = job;
		this.trigger = trigger;
	}
	public JobDetail getJob() {
		return job;
	}
	public void setJob(JobDetail job) {
		this.job = job;
	}
	public Trigger getTrigger() {
		return trigger;
	}
	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}
	
}
