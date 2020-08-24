package com.jy.rpc.pojo;

import java.io.Serializable;

public class Message implements Serializable {
	
	private int id;
	
	private String methodName;
	
	private String invokeClass;
	
	private Object[] args;
	
	private Object response;
	
	public Object getResponse() {
		return response;
	}
	
	public void setResponse(Object response) {
		this.response = response;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	public String getInvokeClass() {
		return invokeClass;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setInvokeClass(String invokeClass) {
		this.invokeClass = invokeClass;
	}
	
	public Object[] getArgs() {
		return args;
	}
	
	public void setArgs(Object[] args) {
		this.args = args;
	}
	
	
	
	public Message(String invokeClass, String methodName, Object[] args, int id) {
		this.invokeClass = invokeClass;
		this.methodName = methodName;
		this.args = args;
		this.id = id;
	}
}
