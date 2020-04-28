package com.swingfrog.summer.loader;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;

public class JarLoader {

	protected static Method addURL = null;
	static {
		try {
			addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			addURL.setAccessible(true);
		} catch (Exception ignored) {
			
		}
	}

	public static void loadJar(String path) throws Exception {
		loadJar(new File(path));
	}
	
	public static void loadJar(File jarFile) throws Exception {
		loadJar(jarFile.toURI().toURL());
	}
	
	public static void loadJar(URL url) throws Exception {
		addURL.invoke(ClassLoader.getSystemClassLoader(), url);
	}
	
	public static void loadJarByDir(File dir) throws Exception {
		if (dir.isDirectory()) {
			for (File file : Objects.requireNonNull(dir.listFiles())) {
				loadJarByDir(file);
			}
		} else {
			if (dir.getName().endsWith(".jar")) {
				loadJar(dir);
			}
		}
	}
	
}
