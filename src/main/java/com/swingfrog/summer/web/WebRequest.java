package com.swingfrog.summer.web;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.swingfrog.summer.protocol.SessionRequest;

import com.swingfrog.summer.server.RemoteDispatchMgr;
import io.netty.handler.codec.http.HttpRequest;

public class WebRequest extends SessionRequest {

	private boolean dynamic;
	private String path;
	private Map<String, WebFileUpload> fileUploadMap;
	private HttpRequest httpRequest;
	
	public static WebRequest build(HttpRequest httpRequest, String uri) {
		if (uri.length() == 1) {
			return null;
		}
		WebRequest webRequest = new WebRequest();
		webRequest.setHttpRequest(httpRequest);
		JSONObject data = new JSONObject();
		if (uri.contains("?")) {
			String[] texts = uri.split("\\?");
			if (texts.length == 2) {
				uri = texts[0];
				String query = texts[1];
				if (query.length() > 0) {
					String[] value = query.split("&");
					for (String keyValue : value) {
						if (keyValue.contains("=")) {
							String[] keyAndValue = keyValue.split("=");
							if (keyAndValue.length == 2)
								data.put(keyAndValue[0], keyAndValue[1]);
						}
					}
				}
			}
		}
		webRequest.setPath(uri);
		String remoteMethod = uri.substring(1);
		if (RemoteDispatchMgr.get().containsRemoteMethod(remoteMethod)) {
			webRequest.setDynamic(true);
			webRequest.setRemote(remoteMethod);
			webRequest.setMethod(null);
		} else {
			if (uri.contains(".")) {
				webRequest.setDynamic(false);
			} else {
				webRequest.setDynamic(true);
				String[] remoteMethods = uri.split("_");
				if (remoteMethods.length == 2) {
					webRequest.setRemote(remoteMethods[0].substring(1));
					webRequest.setMethod(remoteMethods[1]);
				} else {
					webRequest.setDynamic(false);
				}
			}
		}
		webRequest.setData(data);
		webRequest.setFileUploadMap(new HashMap<>());
		return webRequest;
	}
	
	public boolean isDynamic() {
		return dynamic;
	}
	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Map<String, WebFileUpload> getFileUploadMap() {
		return fileUploadMap;
	}
	public void setFileUploadMap(Map<String, WebFileUpload> fileUploadMap) {
		this.fileUploadMap = fileUploadMap;
	}
	public HttpRequest getHttpRequest() {
		return httpRequest;
	}
	public void setHttpRequest(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

	@Override
	public String toString() {
		return "WebRequest [remote=" + getRemote() + ", method=" + getMethod() + ", data=" + getData() + "]";
	}
	
}
