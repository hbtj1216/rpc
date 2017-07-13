package com.tao.rpc.context;

import io.netty.channel.Channel;

/**
 * RpcRequest的包装器
 * 用于服务器：
 * 包装RpcRequest和对应的Channel
 * @author Tao
 *
 */

public class RpcRequestWrapper {
	
	private final RpcRequest rpcRequest;	//rpc请求体
	private final Channel channel;			//本请求对应的channel
	
	
	public RpcRequestWrapper(RpcRequest rpcRequest, Channel channel) {
		this.rpcRequest = rpcRequest;
		this.channel = channel;
	}
	
	public int getId() {
		return rpcRequest.getId();
	}
	
	public String getMethodName() {
		return rpcRequest.getMethodName();
	}
	
	public Object[] getArgs() {
		return rpcRequest.getArgs();
	}
	
	public Channel getChannel() {
		return channel;
	}
	
}
