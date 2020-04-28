package com.swingfrog.summer.client;

public interface RemoteCallback {

	void success(Object obj);
	
	void failure(long code, String msg);
	
}
