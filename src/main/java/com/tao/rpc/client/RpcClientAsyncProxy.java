package com.tao.rpc.client;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.tao.rpc.exception.RpcMethodNotFoundException;
import com.tao.rpc.future.RpcFuture;
import com.tao.rpc.utils.InfoPrinter;

/**
 * 异步连接的客户端代理类
 * @author Tao
 *
 */

public class RpcClientAsyncProxy {
	
	private RpcClient rpcClient;
	//方法集合
	private Set<String> serviceMethodSet = new HashSet<>();
	
	
	/**
	 * 构造函数
	 * @param rpcClient
	 * @param interfaceClass
	 */
	public RpcClientAsyncProxy(RpcClient rpcClient, Class<?> interfaceClass) {
		
		this.rpcClient = rpcClient;
		Method[] methods = interfaceClass.getMethods();
		//获取所有的方法名，存入methods
		for(Method method : methods) {
			serviceMethodSet.add(method.getName());
		}
	}
	
	
	/**
	 * 异步远程调用
	 * @param methodName
	 * @param args
	 * @return
	 */
	public RpcFuture call(String methodName, Object ...args) {
		
		//如果不包含这个方法，抛出异常
		if(!serviceMethodSet.contains(methodName)) {
			throw new RpcMethodNotFoundException(methodName);
		}
		
		//通过rpcClient的call方法将调用请求发往服务器端
		RpcFuture rpcFuture = rpcClient.call(methodName, args);
		
		if(rpcFuture != null) {
			return rpcFuture;
		}
		else {
			InfoPrinter.println("Disconnect with the server.");
			return null;
		}
	}
	
}














