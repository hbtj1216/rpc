package com.tao.rpc.server;

import com.tao.rpc.aop.RpcInvokeHook;


/**
 * RpcServer 的构建者 RpcServerBuilder。
 * @author Tao
 *
 */

public class RpcServerBuilder {
	
	private Class<?> interfaceClass;    // 对客户端提供服务的类所实现的接口
	private Object serviceProvider;     // 服务的具体提供类
	
	private int port;
	private int threads;
	private RpcInvokeHook rpcInvokeHook;
	
	
	/**
	 * 创建RpcServerBuilder对象
	 * @return
	 */
	public static RpcServerBuilder create() {
		return new RpcServerBuilder();
	}
	
	
	/**
	 * 设置提供服务的接口
	 * @param interfaceClass
	 * @return
	 */
	public RpcServerBuilder setServiceInterface(Class<?> interfaceClass) {
		this.interfaceClass = interfaceClass;
		return this;
	}
	
	
	/**
	 * 设置服务的提供者
	 * @param serviceProvider
	 * @return
	 */
	public RpcServerBuilder setServiceProvider(Object serviceProvider) {
		this.serviceProvider = serviceProvider;
		return this;
	}
	
	
	/**
	 * 绑定端口号
	 * @param port
	 * @return
	 */
	public RpcServerBuilder bind(int port) {
		this.port = port;
		return this;
	}
	
	
	/**
	 * 设置处理每个客户端发来的RpcRequest的线程池中的线程的数量。
	 * @param threads
	 * @return
	 */
	public RpcServerBuilder setThreads(int threads) {
		this.threads = threads;
		return this;
	}
	
	
	/**
	 * 设置钩子
	 * @param rpcInvokeHook
	 * @return
	 */
	public RpcServerBuilder setHook(RpcInvokeHook rpcInvokeHook) {
		this.rpcInvokeHook = rpcInvokeHook;
		return this;
	}
	
	
	/**
	 * 构建RpcServer对象。
	 * @return
	 */
	public RpcServer build() {
		
		if(this.threads <= 0) {
			//默认值
			this.threads = Runtime.getRuntime().availableProcessors();
		}
		
		//创建RpcServer对象
		RpcServer rpcServer = new RpcServer(this.interfaceClass, this.serviceProvider,
											this.port, this.threads, this.rpcInvokeHook);
		
		return rpcServer;
	} 
	
	
}









