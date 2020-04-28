package com.swingfrog.summer.web.view;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedInput;

public class FileView implements WebView {

	private RandomAccessFile file;
	private String contentType;
	private volatile Map<String, String> headers;

	public static FileView of(String fileName) throws IOException {
		return new FileView(fileName);
	}
	
	public FileView(String fileName) throws IOException {
		File f = new File(fileName);
		file = new RandomAccessFile(f, "r");
		contentType = Files.probeContentType(Paths.get(f.getName()));
		if (contentType == null) {
			MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
			contentType = mimeTypesMap.getContentType(f.getName());
		}
	}
	
	@Override
	public void ready() {

	}
	
	@Override
	public int getStatus() {
		return 200;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public long getLength() throws IOException {
		return file.length();
	}

	@Override
	public ChunkedInput<ByteBuf> getChunkedInput() throws IOException {
		return new ChunkedFile(file);
	}
	
	@Override
	public String toString() {
		return "FileView";
	}

	public void addHeader(String key, String value) {
		if (headers == null) {
			synchronized (this) {
				if (headers == null) {
					headers = Maps.newConcurrentMap();
				}
			}
		}
		headers.put(key, value);
	}

	@Override
	public Map<String, String> getHeaders() {
		return headers;
	}

}
