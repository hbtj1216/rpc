package com.tao.rpc.domain;

/**
 * rpc request 请求体
 * 包括：id, 方法名, 方法参数表
 * @author Tao
 *
 */

public class RpcRequest {
	
	private final int id;				//id
	private final String methodName;	//方法名
	private final Object[] args;		//方法参数表
	
	public RpcRequest(int id, String methodName, Object[] args) {
		this.id = id;
		this.methodName = methodName;
		this.args = args;
	}

	public int getId() {
		return id;
	}

	public String getMethodName() {
		return methodName;
	}

	public Object[] getArgs() {
		return args;
	}
	
}










