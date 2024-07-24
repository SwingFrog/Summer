package com.swingfrog.summer.web;

import java.io.IOException;
import java.io.RandomAccessFile;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.multipart.FileUpload;

public class WebFileUpload {

	private final FileUpload fileUpload;
	
	WebFileUpload(FileUpload fileUpload) {
		this.fileUpload = fileUpload;
	}
	
	public String getFileName() {
		return fileUpload.getFilename();
	}
	
	public ByteBuf getByteBuf() {
		return fileUpload.content();
	}

	public boolean isEmpty() {
		return getByteBuf().readableBytes() == 0;
	}

	public FileUpload getFileUpload() {
		return fileUpload;
	}

	public void saveToFile(String path) throws IOException {
		if (isEmpty())
			return;
		try (RandomAccessFile file = new RandomAccessFile(path, "rw")) {
			file.getChannel().write(getByteBuf().nioBuffer());
		}
	}
	
}
