package com.swingfrog.summer.web.view;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.swingfrog.summer.server.ServerContext;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.web.WebContentTypes;
import com.swingfrog.summer.web.WebRequest;
import com.swingfrog.summer.web.view.render.DefaultWebViewRender;
import com.swingfrog.summer.web.view.render.WebViewRender;
import io.netty.channel.DefaultFileRegion;

public class FileView extends AbstractView {

	private final RandomAccessFile file;
	private String contentType;

	public static FileView of(String fileName) throws IOException {
		return new FileView(fileName);
	}
	
	public FileView(String fileName) throws IOException {
		File f = new File(fileName);
		file = new RandomAccessFile(f, "r");
		contentType = WebContentTypes.getByFileName(f.getName());
		if (contentType == null) {
			contentType = "application/octet-stream";
		}
	}

	@Override
	public WebViewRender onRender(ServerContext serverContext, SessionContext sctx, WebRequest request) throws Exception {
		return new DefaultWebViewRender(new DefaultFileRegion(file.getChannel(), 0, file.length()));
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public String toString() {
		return "FileView";
	}

}
