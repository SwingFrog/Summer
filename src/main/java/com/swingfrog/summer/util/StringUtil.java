package com.swingfrog.summer.util;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {
	
	public static String getString(Object ...args) {
		StringBuilder b = new StringBuilder();
		int len = args.length - 1;
		for (int i = 0; i < len; i++) {
			b.append(args[i]);
			b.append("-");
		}
		b.append(args[len]);
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
