package com.tao.rpc.server;

import java.util.concurrent.BlockingQueue;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.tao.rpc.aop.RpcInvokeHook;
import com.tao.rpc.domain.RpcRequestWrapper;
import com.tao.rpc.domain.RpcResponse;

import io.netty.channel.Channel;

/**
 * RpcServerRequestHandlerRunnable是一个实现了Runnable接口的类。
 * 每个RpcServerRequestHandlerRunnable对象占用一个线程。
 * 主要负责对每个RpcRequest进行处理。
 * @author Tao
 *
 */

public class RpcServerRequestHandlerTask implements Runnable {
	
	private Class<?> interfaceClass;			//接口
	private Object serviceProvider;				//接口的实现类
	private RpcInvokeHook rpcInvokeHook;		//AOP钩子
	private BlockingQueue<RpcRequestWrapper> requestQueue;	//请求任务的阻塞队列
	private RpcRequestWrapper rpcRequestWrapper;

	//reflectasm提供的asm工具类，可以实现比java反射更快的性能
	private MethodAccess methodAccess;

	//上一个被调用的方法的名称
	private String lastMethodName = "";
	//上一个被调用的方法的index, 因为asm生成的访问类会将方法缓存起来，可以通过索引获取
	private int lastMethodIndex;
	
	
	//构造函数	
	public RpcServerRequestHandlerTask(
				Class<?> interfaceClass, 
				Object serviceProvider,
				RpcInvokeHook rpcInvokeHook, 
				BlockingQueue<RpcRequestWrapper> requestQueue) {
		
		this.interfaceClass = interfaceClass;
		this.serviceProvider = serviceProvider;
		this.rpcInvokeHook = rpcInvokeHook;
		this.requestQueue = requestQueue;

		//通过reflectasm提供的MethodAccess类的静态方法生成接口的访问类对象methodAccess
		this.methodAccess = MethodAccess.get(this.interfaceClass);
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
				
				//进行实际的方法调用，并获得结果
				Object result = null;
				if(!methodName.equals(lastMethodName)) {
                    /**
                     * 注意：
                     * 在重复访问方法或者属性的时候，最好通过缓存索引来提取方法或者属性，
                     * 这样能够提高重复访问的性能。
                     */
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
