package com.swingfrog.summer.lifecycle;

public class LifecycleInfo {

	private String name;
	private int priority;
	
	public static LifecycleInfo build(String name, int priority) {
		LifecycleInfo li = new LifecycleInfo();
		li.setName(name);
		li.setPriority(priority);
		return li;
	}
	public static LifecycleInfo build(String name) {
		return build(name, 0);
	}
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	@Override
	public String toString() {
		return "LifecycleInfo [name=" + name + ", priority=" + priority + "]";
	}
	
}
