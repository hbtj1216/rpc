package com.tao.rpc.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import com.tao.rpc.aop.RpcInvokeHook;
import com.tao.rpc.context.RpcRequestWrapper;

/**
 * RpcServer端，对收到的RpcRequest进行处理的handler。
 * @author Tao
 *
 */

public class RpcServerRequestHandler {
	
	private Class<?> interfaceClass;		//Class 对象
	private Object serviceProvider;			//服务提供者
	private RpcInvokeHook rpcInvokeHook;	//钩子
	
	private int threads;	//线程数量
	private ExecutorService threadPool;	//线程池
	
	//任务队列(阻塞队列)
	private BlockingQueue<RpcRequestWrapper> requestQueue = new LinkedBlockingDeque<>();
	
	
	//构造函数
	public RpcServerRequestHandler(
				Class<?> interfaceClass,
				Object serviceProvider,
				int threads,
				RpcInvokeHook rpcInvokeHook) {

		this.interfaceClass = interfaceClass;
		this.serviceProvider = serviceProvider;
		this.threads = threads;
		this.rpcInvokeHook = rpcInvokeHook;
	}
	
	
	/**
	 * 开启线程池，处理RpcRequest请求
	 */
	public void start() {
		threadPool = Executors.newFixedThreadPool(threads);
		for(int i = 0; i < threads; i++) {
			threadPool.execute(new RpcServerRequestHandlerRunnable(interfaceClass, 
					serviceProvider, rpcInvokeHook, requestQueue));
		}
	}
	
	
	/**
	 * 添加RpcRequest请求到任务队列中
	 * @param rpcRequestWrapper
	 */
	public void addRequest(RpcRequestWrapper rpcRequestWrapper) {
		try {
			requestQueue.put(rpcRequestWrapper);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	
}













