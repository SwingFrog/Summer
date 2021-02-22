package com.swingfrog.summer.web;

import java.io.File;
import java.io.IOException;

import com.swingfrog.summer.web.token.WebTokenHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swingfrog.summer.web.view.InteriorViewFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class WebMgr {
	
	private static final Logger log = LoggerFactory.getLogger(WebMgr.class);
	
	public static final String DEFAULT_FAVICON = "/favicon.ico";
	private String index;
	private String favicon;
	private String webContentPath;
	private String templatePath;
	private InteriorViewFactory interiorViewFactory;
	private volatile Configuration freeMarkerConfig;
	private WebTokenHandler webTokenHandler;
	
	private static class SingleCase {
		public static final WebMgr INSTANCE = new WebMgr();
	}
	
	private WebMgr() {
		index = "index.html";
		favicon = "favicon.ico";
		webContentPath = "WebContent";
		templatePath = "Template";
		interiorViewFactory = new InteriorViewFactory();
	}
	
	public static WebMgr get() {
		return SingleCase.INSTANCE;
	}
	
	private void reloadTemplate() {
		freeMarkerConfig = new Configuration(Configuration.VERSION_2_3_23);
		freeMarkerConfig.setDefaultEncoding("UTF-8");
		try {
			freeMarkerConfig.setDirectoryForTemplateLoading(new File(templatePath));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public Template getTemplate(String templateName) throws IOException {
		if (freeMarkerConfig == null) {
			synchronized (this) {
				if (freeMarkerConfig == null) {
					reloadTemplate();
				}
			}
		}
		return freeMarkerConfig.getTemplate(templateName);
	}

	public String getWebContentPath() {
		return webContentPath;
	}

	public void setWebContentPath(String webContentPath) {
		this.webContentPath = webContentPath;
	}

	public String getTemplatePath() {
		return templatePath;
	}

	public synchronized void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
		reloadTemplate();
	}

	public InteriorViewFactory getInteriorViewFactory() {
		return interiorViewFactory;
	}

	public void setInteriorViewFactory(InteriorViewFactory interiorViewFactory) {
		this.interiorViewFactory = interiorViewFactory;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getFavicon() {
		return favicon;
	}

	public void setFavicon(String favicon) {
		this.favicon = favicon;
	}

	public WebTokenHandler getWebTokenHandler() {
		return webTokenHandler;
	}

	public void setWebTokenHandler(WebTokenHandler webTokenHandler) {
		this.webTokenHandler = webTokenHandler;
	}

}
