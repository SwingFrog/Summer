package com.swingfrog.summer.test.quick;

import com.swingfrog.summer.annotation.CronTask;
import com.swingfrog.summer.annotation.IntervalTask;
import com.swingfrog.summer.annotation.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Task
public class QuickTask {

	private static final Logger log = LoggerFactory.getLogger(QuickTask.class);

	@CronTask("0/10 * * * * ? ")
	public void onTenSec() {
		log.info("onTenSec");
	}

	@IntervalTask(value = 5000, nextMinuteBegin = true)
	public void onThreeSec() {
		log.info("onThreeSec");
	}

}
