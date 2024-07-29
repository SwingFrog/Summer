package com.swingfrog.summer.client;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

public interface ClientRemote {
	
	void asyncRemote(String remote, String method, Object data, RemoteCallback remoteCallback);

	void retryAsyncRemote(String remote, String method, Object data, RemoteCallback remoteCallback, long afterTimeRetry, TimeUnit unit);

	<T> T syncRemote(String remote, String method, Object data, Type type);

	<T> T rsyncRemote(String remote, String method, Object data, Type type);
	
}
