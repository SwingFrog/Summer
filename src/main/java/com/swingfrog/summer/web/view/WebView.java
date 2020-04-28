package com.swingfrog.summer.web.view;

import java.io.IOException;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.handler.stream.ChunkedInput;

public interface WebView {

	void ready() throws Exception;
	int getStatus();
	String getContentType();
	long getLength() throws IOException;
	ChunkedInput<ByteBuf> getChunkedInput() throws IOException;
	default Map<String, String> getHeaders() {
		return null;
	}

}
