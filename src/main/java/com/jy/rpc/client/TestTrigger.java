package com.jy.rpc.client;

import com.jy.rpc.aspect.UserService;
import com.jy.rpc.pojo.User;

public class TestTrigger {
	
	public static void main(String[] args) {
		Rpc rpc = Rpc.getInstance();
		UserService userService = rpc.create(UserService.class);
		User user = userService.getUser();
		System.out.print(user);
	}
}
