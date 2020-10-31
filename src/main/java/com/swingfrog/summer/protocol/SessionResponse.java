package com.swingfrog.summer.protocol;

import java.util.Objects;

import com.alibaba.fastjson.JSON;
import com.swingfrog.summer.server.exception.CodeException;
import com.swingfrog.summer.server.exception.CodeMsg;

public class SessionResponse {

	private int code;
	private long id;
	private String remote;
	private String method;
	private Object data;
	private long time;
	
	public static SessionResponse buildPush(String remote, String method, Object data) {
		SessionResponse sr = new SessionResponse();
		sr.setId(0);
		sr.setRemote(remote);
		sr.setMethod(method);
		sr.setData(data);
		sr.setTime(System.currentTimeMillis());
		return sr;
	}
	
	public static SessionResponse buildMsg(long id, String remote, String method, Object data) {
		SessionResponse sr = new SessionResponse();
		sr.setId(id);
		sr.setRemote(remote);
		sr.setMethod(method);
		sr.setData(data);
		sr.setTime(System.currentTimeMillis());
		return sr;
	}
	
	public static SessionResponse buildMsg(SessionRequest req, Object data) {
		return buildMsg(req.getId(), req.getRemote(), req.getMethod(), data);
	}

	public static SessionResponse buildError(long id, String remote, String method, int code, String msg) {
		SessionResponse sr = new SessionResponse();
		sr.setCode(code);
		sr.setId(id);
		sr.setRemote(remote);
		sr.setMethod(method);
		sr.setData(msg);
		sr.setTime(System.currentTimeMillis());
		return sr;
	}
	
	public static SessionResponse buildError(long id, String remote, String method, CodeException e) {
		return buildError(id, remote, method, e.getCode(), e.getMsg());
	}
	
	public static SessionResponse buildError(SessionRequest req, CodeException e) {
		return buildError(req.getId(), req.getRemote(), req.getMethod(), e);
	}
	
	public static SessionResponse buildError(long id, String remote, String method, CodeMsg e) {
		return buildError(id, remote, method, e.getCode(), e.getMsg());
	}
	
	public static SessionResponse buildError(SessionRequest req, CodeMsg e) {
		return buildError(req.getId(), req.getRemote(), req.getMethod(), e);
	}

	public static SessionResponse buildError(SessionRequest req, int code, String msg) {
		return buildError(req.getId(), req.getRemote(), req.getMethod(), code, msg);
	}
	
	public String toJSONString() {
		return JSON.toJSONString(this);
	}

	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
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
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SessionResponse that = (SessionResponse) o;
		return id == that.id &&
				Objects.equals(remote, that.remote) &&
				Objects.equals(method, that.method);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, remote, method);
	}

}
