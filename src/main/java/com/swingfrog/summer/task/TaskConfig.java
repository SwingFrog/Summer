package com.swingfrog.summer.task;

public class TaskConfig {

	private int coreThread;

	public int getCoreThread() {
		return coreThread;
	}

	public void setCoreThread(int coreThread) {
		this.coreThread = coreThread;
	}

	@Override
	public String toString() {
		return "TaskConfig{" +
				"coreThread=" + coreThread +
				'}';
	}

}
