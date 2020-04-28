package com.swingfrog.summer.ioc;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.TreeMap;

import com.google.common.collect.Maps;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

public class MethodParameterName {

	private CtClass ct;
	
	public MethodParameterName(Class<?> c) throws NotFoundException {
		ct = ClassPool.getDefault().get(c.getName());
	}
	
	public String[] getParameterNameByMethod(Method method) throws NotFoundException {
		CtMethod ctm = ct.getDeclaredMethod(method.getName());
		MethodInfo methodInfo = ctm.getMethodInfo();
		CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		LocalVariableAttribute attr = (LocalVariableAttribute)codeAttribute.getAttribute(LocalVariableAttribute.tag);
		String[] params = null;
		if (attr != null) {
			int len = ctm.getParameterTypes().length;
			params = new String[len];
			TreeMap<Integer, String> sortMap = Maps.newTreeMap();
			for (int i = 0; i < attr.tableLength(); i++)
				sortMap.put(attr.index(i), attr.variableName(i));
			int pos = Modifier.isStatic(ctm.getModifiers()) ? 0 : 1;
			params = Arrays.copyOfRange(sortMap.values().toArray(new String[0]), pos, params.length + pos);
		}
		return params;
	}
	
}
