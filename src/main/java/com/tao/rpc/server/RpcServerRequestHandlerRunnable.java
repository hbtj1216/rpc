package com.tao.rpc.server;

import java.util.concurrent.BlockingQueue;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.tao.rpc.aop.RpcInvokeHook;
import com.tao.rpc.context.RpcRequestWrapper;
import com.tao.rpc.context.RpcResponse;

import io.netty.channel.Channel;

/**
 * RpcServerRequestHandlerRunnable是一个实现了Runnable接口的类。
 * 每个RpcServerRequestHandlerRunnable对象占用一个线程。
 * 主要负责对每个RpcRequest进行处理。
 * @author Tao
 *
 */

public class RpcServerRequestHandlerRunnable implements Runnable {
	
	private Class<?> interfaceClass;
	private Object serviceProvider;
	private RpcInvokeHook rpcInvokeHook;
	private BlockingQueue<RpcRequestWrapper> requestQueue;
	private RpcRequestWrapper rpcRequestWrapper;
	
	private MethodAccess methodAccess;
	private String lastMethodName = "";
	private int lastMethodIndex;
	
	
	//构造函数	
	public RpcServerRequestHandlerRunnable(
				Class<?> interfaceClass, 
				Object serviceProvider,
				RpcInvokeHook rpcInvokeHook, 
				BlockingQueue<RpcRequestWrapper> requestQueue) {
		
		this.interfaceClass = interfaceClass;
		this.serviceProvider = serviceProvider;
		this.rpcInvokeHook = rpcInvokeHook;
		this.requestQueue = requestQueue;
		
		methodAccess = MethodAccess.get(this.interfaceClass);
	}


	/**
	 * 每个RpcServerRequestHandlerRunnable线程执行的任务
	 */
	@Override
	public void run() {
		
		while(true) {
			try {
				
				//从阻塞队列的头部取出任务
				rpcRequestWrapper = requestQueue.take();
				
				//提取调用方法的：方法名、参数
				String methodName = rpcRequestWrapper.getMethodName();
				Object[] args = rpcRequestWrapper.getArgs();
				
				if(rpcInvokeHook != null) {
					rpcInvokeHook.beforeInvoke(methodName, args);
				}
				
				//进行实际的方法调用，病获得结果
				Object result = null;
				if(!methodName.equals(lastMethodName)) {
					lastMethodIndex = methodAccess.getIndex(methodName);
					lastMethodName = methodName;
				}
				
				//在这里，由具体的serviceProvider对象去执行具体调用的方法。通过反射完成。
				result = methodAccess.invoke(serviceProvider, lastMethodIndex, args);
				//获得对应的channel
				Channel channel = rpcRequestWrapper.getChannel();
				//获得对应的id
				int id = rpcRequestWrapper.getId();
				//组装成一个RpcResponse对象
				RpcResponse rpcResponse = new RpcResponse(id, result, true);
				//发回给客户端
				channel.writeAndFlush(rpcResponse);
				
				if(rpcInvokeHook != null) {
					rpcInvokeHook.afterInvoke(methodName, args);
				}
				
			} catch (Exception e) {
				Channel channel = rpcRequestWrapper.getChannel();
				int id = rpcRequestWrapper.getId();
				//发回异常给客户端
				RpcResponse rpcResponse = new RpcResponse(id, e, false);
				channel.writeAndFlush(rpcResponse);
			}
		}
		
	}

}
