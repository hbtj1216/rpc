package com.tao.rpc.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.tao.rpc.context.RpcRequest;


/**
 * 使用Kryo对RpcRequest对象序列化。
 * @author Tao
 *
 */
public class RpcRequestSerializer extends Serializer<RpcRequest> {
	
	
	/**
	 * 字节序转RpcRequest对象
	 */
	@Override
	public RpcRequest read(Kryo kryo, Input input, Class<RpcRequest> type) {
		
		int id = input.readInt();									//id
		byte methodLength = input.readByte();						//方法名长度
		byte[] methodBytes = input.readBytes(methodLength);
		String methodName = new String(methodBytes);				//方法名
		Object[] args = (Object[]) kryo.readClassAndObject(input);	//方法参数
		
		//组合并创建一个rpcRequest
		RpcRequest rpcRequest = new RpcRequest(id, methodName, args);
		return rpcRequest;
	}

	
	/**
	 * RpcRequest对象转字节序列
	 * 输出的数据结构：
	 * <int><byte><byte[]><Object[]>
	 */
	@Override
	public void write(Kryo kryo, Output output, RpcRequest rpcRequest) {
		
		output.writeInt(rpcRequest.getId());					//id
		output.writeByte(rpcRequest.getMethodName().length());	//方法名的长度
		output.write(rpcRequest.getMethodName().getBytes());	//方法名
		kryo.writeClassAndObject(output, rpcRequest.getArgs());	//方法参数
	}

}
