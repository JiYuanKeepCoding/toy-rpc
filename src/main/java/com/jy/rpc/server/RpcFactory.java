package com.jy.rpc.server;

import java.util.HashMap;
import java.util.Map;

public class RpcFactory {
	
	private static Map<String, Object> factory = new HashMap<>();
	
	public static void register(String className, Object obj) {
		factory.put(className, obj);
	}
	
	public static  <T> T get(String className) {
		return (T)factory.get(className);
	}
}
