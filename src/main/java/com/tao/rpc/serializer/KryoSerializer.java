package com.tao.rpc.serializer;

import java.io.IOException;

import com.esotericsoftware.kryo.Kryo;



import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

public class KryoSerializer {

	// 预留4字节储存长度信息
	private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

	
	/**
	 * 序列化: java对象转字节序列
	 * 
	 * @param object
	 * @param byteBuf
	 */
	public static void serialize(Object object, ByteBuf byteBuf) {
		
		Kryo kryo = KryoHolder.get();
		int startIndex = byteBuf.writerIndex();
		ByteBufOutputStream byteBufOutputStream = new ByteBufOutputStream(byteBuf);
		try {
			byteBufOutputStream.write(LENGTH_PLACEHOLDER);
			Output output = new Output(1024*4, -1);
			output.setOutputStream(byteBufOutputStream);
	        kryo.writeClassAndObject(output, object);
	        
	        output.flush();
	        output.close();
	        
	        int endIndex = byteBuf.writerIndex();
	        byteBuf.setInt(startIndex, endIndex - startIndex - 4);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 反序列化: 字节序列转java对象
	 * 
	 * @param byteBuf
	 * @return
	 */
	public static Object deserialize(ByteBuf byteBuf) {
		if(byteBuf == null)
            return null;
		
        Input input = new Input(new ByteBufInputStream(byteBuf));
        Kryo kryo = KryoHolder.get();
        return kryo.readClassAndObject(input);
	}

}
