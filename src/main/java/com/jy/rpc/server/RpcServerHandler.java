package com.jy.rpc.server;

import com.jy.rpc.pojo.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public class RpcServerHandler extends ChannelInboundHandlerAdapter {
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		Message message = (Message)msg;
		try {
			Class[] paramClasses = new Class[0];
			if (message.getArgs()  != null) {
				paramClasses = Arrays.asList(message.getArgs()).stream()
					.map(Object::getClass).collect(Collectors.toList()).toArray(new Class[0]);
			}
			Method method = Class.forName(message.getInvokeClass()).
				getDeclaredMethod(message.getMethodName(), paramClasses);
			Object result = method.invoke(RpcFactory.get(message.getInvokeClass()), message.getArgs());
			message.setResponse(result);
			ctx.write(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
