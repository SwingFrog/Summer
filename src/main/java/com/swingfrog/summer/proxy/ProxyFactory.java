package com.swingfrog.summer.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class ProxyFactory implements MethodInterceptor {

	private Object target;
	private ProxyMethodInterceptor interceptor;
	
	private ProxyFactory(Object target, ProxyMethodInterceptor interceptor) {
		this.target = target;
		this.interceptor = interceptor;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getProxyInstance(Object target, ProxyMethodInterceptor interceptor) {
		ProxyFactory pf = new ProxyFactory(target, interceptor);
		Enhancer en = new Enhancer();
		en.setSuperclass(target.getClass());
		en.setCallback(pf);
		return (T)en.create();
	}
	
	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		return interceptor.intercept(target, method, args);
	}
}
