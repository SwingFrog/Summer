package com.swingfrog.summer.util;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {
	
	public static String getString(Object ...args) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < args.length - 1; i++) {
			b.append(args[i]);
			b.append("-");
		}
		b.append(args[args.length - 1]);
		return b.toString().intern();
	}

	public static List<String> findKey(String content, String start, String end) {
		List<String> list = new ArrayList<>();
		int index = -1;
		while ((index = content.indexOf(start, index)) > -1) {
			index += start.length();
			int endIndex = content.indexOf(end, index);
			list.add(content.substring(index, endIndex));
		}
		return list;
	}
}
