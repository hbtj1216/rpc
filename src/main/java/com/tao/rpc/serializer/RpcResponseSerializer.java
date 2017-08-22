package com.tao.rpc.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.tao.rpc.domain.RpcResponse;


/**
 * 使用Kryo实现序列化和反序列化
 * @author Tao
 *
 */

public class RpcResponseSerializer extends Serializer<RpcResponse> {
	
	
	/**
	 * 字节序列转RpcResponse对象
	 */
	@Override
	public RpcResponse read(Kryo kryo, Input input, Class<RpcResponse> type) {
		
		int id = input.readInt();
		boolean isInvokeSuccess = input.readBoolean();
		Object resultOrThrowable = kryo.readClassAndObject(input);
		
		//创建RpcResponse对象
		RpcResponse rpcResponse = new RpcResponse(id, resultOrThrowable, isInvokeSuccess);	
		return rpcResponse;
	}
	
	
	/**
	 * RpcResponse对象转字节序列
	 * <int><boolean><Object>
	 */
	@Override
	public void write(Kryo kryo, Output output, RpcResponse rpcResponse) {
		
		output.writeInt(rpcResponse.getId());
		output.writeBoolean(rpcResponse.isInvokeSuccess());
		if(rpcResponse.isInvokeSuccess()) {
			kryo.writeClassAndObject(output, rpcResponse.getResult());
		}
		else {
			kryo.writeClassAndObject(output, rpcResponse.getThrowable());
		}
	}
	
	
}










