package com.jy.rpc.client;

import com.jy.rpc.pojo.Message;
import com.jy.rpc.serialize.MessageDeserializer;
import com.jy.rpc.serialize.MessageSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;

public class Rpc {
	
	private String ip = "127.0.0.1";
	
	private int port = 4021;
	
	private Bootstrap b;
	
	private ConnectionPool connectionPool;
	
	private Rpc() {
	
	}
	
	public static synchronized Rpc getInstance() {
		Rpc rpc = new Rpc();
		EventLoopGroup workerGroup = new NioEventLoopGroup(); //client端一个多路复用器就够了
		Bootstrap bootstrap = new Bootstrap();
		rpc.b = bootstrap; // (1)
		rpc.b.group(workerGroup); // (2)
		rpc.b.channel(NioSocketChannel.class); // (3)
		rpc.b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
		rpc.b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new MessageSerializer());
				ch.pipeline().addLast(new MessageDeserializer());
			}
		});
		rpc.connectionPool = new ConnectionPool(bootstrap);
		return rpc;
	}
	
	public <T> T create(Class<T> t) {
		Object proxyInstance =  Proxy.newProxyInstance(
			t.getClassLoader(),
			new Class[] { t },
			(Object proxy, Method method, Object[] args) -> {
				if (method.getName().equals("toString")) {
					return null;
				}
				Channel channel = this.connectionPool.getConnection(new InetSocketAddress(this.port));
				int id = new Random().nextInt(Integer.MAX_VALUE);
				channel.write(
					new Message(method.getDeclaringClass().getName(), method.getName(), args, id)
				).sync();
				Thread currentThread = Thread.currentThread();
				channel.attr(AttributeKey.valueOf(String.valueOf(id))).set(currentThread);
				LockSupport.park();
				return channel.attr(AttributeKey.valueOf(String.valueOf(id))).get();
			}
		);
		return (T)proxyInstance;
	}
}