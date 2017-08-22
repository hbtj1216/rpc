package com.tao.rpc.aop;

/**
 * RpcInvokeHook接口中的方法用于记录调用(invoke)的次数或者日志。
 * @author Tao
 *
 */

public interface RpcInvokeHook {
	
	/**
	 * 方法调用之前,添加的操作
	 * @param methodName
	 * 			方法名
	 * @param args
	 * 			方法的参数
	 */
	public void beforeInvoke(String methodName, Object[] args);
	
	/**
	 * 方法调用之后,添加的操作
	 * @param methodName
	 * 			方法名
	 * @param args
	 * 			方法的参数
	 */
	public void afterInvoke(String methodName, Object[] args);
	
}
