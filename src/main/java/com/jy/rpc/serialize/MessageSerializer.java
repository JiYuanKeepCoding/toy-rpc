package com.jy.rpc.serialize;

import com.jy.rpc.pojo.Message;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;

public class MessageSerializer extends ChannelOutboundHandlerAdapter {
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
		try (ByteOutputStream bos = new ByteOutputStream()) {
			Message m = (Message) msg;
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(m);
			ByteBuf byteBuf = Unpooled.buffer();
			byteBuf.writeInt(m.getId());
			byteBuf.writeInt(bos.getBytes().length);
			byteBuf.writeBytes(bos.getBytes());
			ctx.write(byteBuf, promise);
			ctx.flush();
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
