package com.swingfrog.summer.concurrent;

import java.util.List;

import com.swingfrog.summer.util.StringUtil;

public class MatchGroupKey {

	private final String mainKey;
	private List<String> keys;
	private String waitFormat;
	
	public MatchGroupKey(String mainKey) {
		this.mainKey = mainKey;
		if (mainKey != null && !mainKey.isEmpty()) {
			keys = StringUtil.findKey(mainKey, "${", "}");
			waitFormat = mainKey.replaceAll("(\\$\\{[a-zA-Z]*\\})", "%s");
		}
	}
	
	public boolean hasKeys() {
		return keys != null && !keys.isEmpty();
	}
	
	public List<String> getKeys() {
		return keys;
	}
	
	public String getMainKey() {
		return mainKey;
	}
	
	public String getMainKey(Object ...args) {
		if (waitFormat != null) {			
			return String.format(waitFormat, args);
		}
		return mainKey;
	}

}