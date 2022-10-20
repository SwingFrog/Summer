package com.swingfrog.summer.task;

import com.swingfrog.summer.task.cron.CronSequenceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TaskTrigger {

	private static final Logger log = LoggerFactory.getLogger(TaskTrigger.class);

	private final CronSequenceGenerator cron;

	// interval: interval[0], delay: interval[1], nextMinuteBegin: interval[2]
	private final long[] interval;

	private final Runnable runnable;

	private ScheduledFuture<?> scheduledFuture;

	private volatile boolean shutdown;

	private TaskTrigger(CronSequenceGenerator cron, long[] interval, Runnable runnable) {
		this.cron = cron;
		this.interval = interval;
		this.runnable = runnable;
	}

	public static TaskTrigger ofCron(CronSequenceGenerator cron, Runnable runnable) {
		return new TaskTrigger(cron, null, runnable);
	}

	public static TaskTrigger ofInterval(long interval, long delay, boolean nextMinuteBegin, Runnable runnable) {
		return new TaskTrigger(null, new long[] {interval, delay, nextMinuteBegin ? 1 : 0}, runnable);
	}

	public CronSequenceGenerator getCron() {
		return cron;
	}

	public long getInterval() {
		if (interval == null) {
			return -1;
		}
		return interval[0];
	}

	public long getDelay() {
		if (interval == null) {
			return -1;
		}
		return interval[1];
	}

	public boolean isNextMinuteBegin() {
		if (interval == null) {
			return false;
		}
		return interval[2] > 0;
	}

	public boolean isCron() {
		return cron != null;
	}

	public boolean isInterval() {
		return interval != null;
	}

	public Runnable getRunnable() {
		return runnable;
	}

	void start(ScheduledExecutorService executor) {
		if (cron == null) {
			long delay = getDelay();
			if (isNextMinuteBegin()) {
				long time = System.currentTimeMillis();
				long minuteMs = TimeUnit.MINUTES.toMillis(1);
				long nextMinuteTime = (time / minuteMs + 1) * minuteMs;
				delay += nextMinuteTime - time;
			}
			scheduledFuture = executor.scheduleAtFixedRate(() -> {
				try {
					runnable.run();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}, delay, getInterval(), TimeUnit.MILLISECONDS);
		} else {
			nextCron(executor);
		}
	}

	private void nextCron(ScheduledExecutorService executor) {
		Date date = new Date();
		Date next = cron.next(date);
		scheduledFuture = executor.schedule(() -> {
			try {
				runnable.run();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (!shutdown) {
				nextCron(executor);
			}
		}, next.getTime() - date.getTime(), TimeUnit.MILLISECONDS);
	}

	public void stop(boolean mayInterruptIfRunning) {
		shutdown = true;
		if (scheduledFuture != null) {
			scheduledFuture.cancel(mayInterruptIfRunning);
		}
	}

}
