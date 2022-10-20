package com.swingfrog.summer.task;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInvoke implements Runnable {
	
	private Object obj;
	private Method method;
	
	public void init(Object obj, Method method) {
		this.obj = obj;
		this.method = method;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	@Override
	public void run() {
		try {
			method.invoke(obj);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getTargetException());
		}
	}

}
