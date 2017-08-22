package com.tao.rpc.netty;


import com.tao.rpc.serializer.KryoSerializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码器:java对象转字节序列，并通过netty传送出去
 * @author Tao
 *
 */

public class NettyKryoEncoder extends MessageToByteEncoder<Object> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out)
			throws Exception {
		
		KryoSerializer.serialize(msg, out);
	}

}
