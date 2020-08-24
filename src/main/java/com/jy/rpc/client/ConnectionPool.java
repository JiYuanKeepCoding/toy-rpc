package com.jy.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ConnectionPool {
	
	private Bootstrap bootstrap;
	
	private Map<SocketAddress, Channel> map = new HashMap<>();
	
	public ConnectionPool(Bootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}
	
	public Channel getConnection(SocketAddress socketAddress) throws InterruptedException {
		Channel channel = map.get(socketAddress);
		if (channel == null || !channel.isActive()) {
			channel = bootstrap.connect(socketAddress).sync().channel();
			map.put(socketAddress, channel);
		}
		return channel;
	}
}
