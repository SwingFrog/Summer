package com.swingfrog.summer.proxy;

import java.lang.reflect.Method;

public interface ProxyMethodInterceptor {

	Object intercept(Object obj, Method method, Object[] args) throws Throwable;
	
}
