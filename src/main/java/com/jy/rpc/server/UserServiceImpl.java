package com.jy.rpc.server;

import com.jy.rpc.aspect.UserService;
import com.jy.rpc.pojo.User;

public class UserServiceImpl implements UserService {
	
	@Override
	public User getUser() {
		User user = new User();
		user.setName("jy");
		user.setAge(8);
		return user;
	}
}
