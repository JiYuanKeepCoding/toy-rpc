package com.jy.rpc.serialize;

import com.jy.rpc.pojo.Message;
import com.jy.rpc.util.ChannelHepler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.concurrent.locks.LockSupport;

public class MessageDeserializer extends ChannelInboundHandlerAdapter {
	
	private static String REAMIN = "remaining_byte_length";
	
	private static String TRANSACTION_ID = "transaction_id";
	
	private static String BODY_LENGTH = "body_length";
	
	private static String MESSAGE_BODY = "message_body";
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ByteBuf m = (ByteBuf) msg;
		Channel channel = ctx.channel();
		try {
			while (m.readableBytes() > 0) {
				State state = getChannelState(channel);
				if (state == State.READING_ID) {
					readIntPart(channel, m, TRANSACTION_ID, State.READING_SIZE);
				} else if (state == State.READING_SIZE) {
					readIntPart(channel, m, BODY_LENGTH, State.READING_BODY);
				} else if (state == State.READING_BODY) {
					readMessagePart(ctx, m, MESSAGE_BODY);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			m.release();
		}
	}
	
	private State getChannelState(Channel channel) {
		if (!channel.hasAttr(AttributeKey.valueOf("State"))) {
			return State.READING_ID;
		}
		return (State)channel.attr(AttributeKey.valueOf("State")).get();
	}
	
	private void setChannelState(Channel channel, State state) {
		channel.attr(AttributeKey.valueOf("State")).set(state);
	}
	
	private void readIntPart(Channel channel, ByteBuf m, String key, State nextState) {
		int remaining = ChannelHepler.getValue(channel, REAMIN, 4);
		byte[] bytes = ChannelHepler.getValue(channel, key, new byte[4]);
		while (remaining > 0 && m.readableBytes() > 0) {
			bytes[4 - remaining] = m.readByte();
			remaining --;
		}
		channel.attr(AttributeKey.valueOf(REAMIN)).set(remaining);
		if (remaining == 0) {
			channel.attr(AttributeKey.valueOf(key)).set(
				ChannelHepler.bytes2Int(bytes)
			);
			channel.attr(AttributeKey.valueOf(REAMIN)).set(null);
			setChannelState(channel, nextState);
		}
	}
	
	private void readMessagePart(ChannelHandlerContext ctx, ByteBuf m, String key) throws Exception {
		Channel channel = ctx.channel();
		int length = (Integer)channel.attr(AttributeKey.valueOf(BODY_LENGTH)).get();
		int remaining = ChannelHepler.getValue(channel, REAMIN, length);
		byte[] bytes = ChannelHepler.getValue(channel, key, new byte[length]);
		while (remaining > 0 && m.readableBytes() > 0) {
			bytes[length - remaining] = m.readByte();
			remaining --;
		}
		channel.attr(AttributeKey.valueOf(key)).set(bytes);
		channel.attr(AttributeKey.valueOf(REAMIN)).set(remaining);
		if (remaining == 0) {
			Message message = (Message)new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
			channel.attr(AttributeKey.valueOf(REAMIN)).set(null);
			setChannelState(channel, State.READING_ID);
			Object value = ChannelHepler.getValue(channel, String.valueOf(message.getId()), null);
			if (value != null) {
				channel.attr(AttributeKey.valueOf(String.valueOf(message.getId()))).set(message.getResponse());
				LockSupport.unpark((Thread)value);
			}
			ctx.fireChannelRead(message);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
	
}

enum State {
	READING_ID,
	READING_SIZE,
	READING_BODY
}