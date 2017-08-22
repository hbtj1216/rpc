package com.tao.rpc.exception;

/**
 * 异常类:Rpc方法调用超时
 * @author Tao
 *
 */

public class RpcTimeoutException extends RuntimeException {

	private static final long serialVersionUID = -3979638191198842080L;
	
	public RpcTimeoutException() {
		super("time out when calling a Rpc Invoke!");
	}
}
