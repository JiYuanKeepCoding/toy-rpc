package com.jy.rpc.util;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class ChannelHepler {
	
	public static <T> T getValue(Channel channel, String key, T defaultValue) {
		if (channel.hasAttr(AttributeKey.valueOf(key)) && channel.attr(AttributeKey.valueOf(key)).get() != null) {
			return (T)channel.attr(AttributeKey.valueOf(key)).get();
		}
		return defaultValue;
	}
	
	public static int bytes2Int(byte[] a) {
		return (a[0]<<24)&0xff000000|
			(a[1]<<16)&0x00ff0000|
			(a[2]<< 8)&0x0000ff00|
			(a[3]<< 0)&0x000000ff;
	}
}
