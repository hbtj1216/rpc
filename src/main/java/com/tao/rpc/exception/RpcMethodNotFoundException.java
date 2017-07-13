package com.tao.rpc.exception;

/**
 * 异常类：RPC方法没有找到
 * @author Tao
 *
 */

public class RpcMethodNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 7195030094283925940L;

	public RpcMethodNotFoundException(String methodName) {
		super("method [" + methodName + "] is is not found in current service interface!");
	}
}
