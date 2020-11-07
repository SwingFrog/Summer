package com.swingfrog.summer.lifecycle;

public interface Lifecycle {

	default LifecycleInfo getInfo() {
		return LifecycleInfo.build(this.getClass().getSimpleName());
	}
	void start();
	void stop();
}
