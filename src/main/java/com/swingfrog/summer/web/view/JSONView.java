package com.swingfrog.summer.web.view;

import com.alibaba.fastjson.JSON;

public class JSONView extends TextView {

	public static JSONView of(JSON json) {
		return new JSONView(json);
	}

	public JSONView(JSON json) {
		super(json.toJSONString());
	}

	@Override
	public String getContentType() {
		return "application/json";
	}

	@Override
	public String toString() {
		return "JSONView";
	}
	
}
