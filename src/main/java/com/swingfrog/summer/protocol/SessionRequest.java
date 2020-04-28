package com.swingfrog.summer.protocol;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class SessionRequest {

	private long id;
	private String remote;
	private String method;
	private JSONObject data;
	
	public static SessionRequest buildRemote(long id, String remote, String method, JSONObject data) {
		SessionRequest sr = new SessionRequest();
		sr.setId(id);
		sr.setRemote(remote);
		sr.setMethod(method);
		sr.setData(data);
		return sr;
	}
	
	public static SessionRequest buildRemote(long id, String remote, String method, Object data) {
		SessionRequest sr = new SessionRequest();
		sr.setId(id);
		sr.setRemote(remote);
		sr.setMethod(method);
		sr.setData((JSONObject) JSON.toJSON(data));
		return sr;
	}
	
	public String toJSONString() {
		return JSON.toJSONString(this);
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getRemote() {
		return remote;
	}
	public void setRemote(String remote) {
		this.remote = remote;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public JSONObject getData() {
		return data;
	}
	public void setData(JSONObject data) {
		this.data = data;
	}

}
