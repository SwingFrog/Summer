package com.swingfrog.summer.web;

import java.io.IOException;
import java.io.RandomAccessFile;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.multipart.FileUpload;

public class WebFileUpload {

	private String fileName;
	private ByteBuf byteBuf;
	
	WebFileUpload(FileUpload fileUpload) {
		fileName = fileUpload.getFilename();
		byteBuf = fileUpload.content();
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public ByteBuf getByteBuf() {
		return byteBuf;
	}
	
	public void saveToFile(String path) throws IOException {
		RandomAccessFile file = new RandomAccessFile(path, "rw");
		file.getChannel().write(byteBuf.nioBuffer());
		file.close();
	}
	
}
