package com.swingfrog.summer.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class RunnableQueue implements Executor {

	private static final int STATUS_WAIT = 1;
	private static final int STATUS_INTEND = 2;
	private static final int STATUS_RUNNING = 3;

	private static final Logger log = LoggerFactory.getLogger(RunnableQueue.class);

	private final AtomicInteger status = new AtomicInteger(STATUS_WAIT);
	private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
	private final Executor executor;
	private volatile boolean active = true;
	
	public RunnableQueue(Executor executor) {
		this.executor = executor;
	}

	@Override
	public void execute(Runnable runnable) {
		if (!active) {
			throw new UnsupportedOperationException("queue is shutdown.");
		}
		Objects.requireNonNull(runnable);
		queue.add(runnable);
		next();
	}

	public void clear() {
		queue.clear();
	}

	public int getSize() {
		return queue.size();
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public void shutdown() {
		active = false;
		try {
			while (!isEmpty()) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	private void next() {
		do {
			if (status.get() == RunnableQueue.STATUS_RUNNING)
				return;
		} while (!status.compareAndSet(RunnableQueue.STATUS_WAIT, RunnableQueue.STATUS_INTEND));

		Runnable runnable = queue.poll();

		if (runnable == null) {
			status.set(RunnableQueue.STATUS_WAIT);
			return;
		}

		status.set(RunnableQueue.STATUS_RUNNING);
		executor.execute(()-> {
			try {
				runnable.run();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				status.set(RunnableQueue.STATUS_WAIT);
				if (!queue.isEmpty())
					next();
			}
		});
	}

}
