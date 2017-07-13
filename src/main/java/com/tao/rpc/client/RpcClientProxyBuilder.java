package com.tao.rpc.client;

import java.lang.reflect.Proxy;

import com.tao.rpc.aop.RpcInvokeHook;

/**
 * RpcClientProxyBuilder 是用于产生 代理对象 的建造者类。
 * 可以生成 同步\异步方式的代理对象
 * @author Tao
 *
 */

public class RpcClientProxyBuilder {
	
	/**
	 * 静态内部类。
	 * @author Tao
	 *
	 * @param <T>
	 */
	public static class ProxyBuilder<T> {
		
		private Class<T> interfaceClass;	//接口的Class对象
		private RpcClient rpcClient;		//RpcClient对象
		
		private String host;	//主机
		private int port;		//端口号
		private long timeoutMills = 0;	//超时时间(毫秒)
		private RpcInvokeHook rpcInvokeHook = null;	//AOP钩子
		private int threads;	//处理请求的线程数量
		
		
		//构造函数
		public ProxyBuilder(Class<T> interfaceClass) {
			this.interfaceClass = interfaceClass;
		}
		
		
		public RpcClient getRpcClient() {
			return this.rpcClient;
		}
		
		
		/**
		 * 设置超时时间，单位(毫秒)。
		 * 0表示不用等待超时，一直等待直到获得结果。只工作在同步方式下。
		 * 默认值: 0
		 * @param timeoutMills
		 * @return
		 */
		public ProxyBuilder<T> setTimeoutMills(long timeoutMills) {
			this.timeoutMills = timeoutMills;
			if(timeoutMills < 0)
				throw new IllegalArgumentException("timeoutMills can not be minus!");
			
			return this;
		}
		
		
		/**
		 * 设置钩子
		 * @param hook
		 * @return
		 */
		public ProxyBuilder<T> setHook(RpcInvokeHook hook) {
			this.rpcInvokeHook = hook;
			return this;
		}
		
		
		/**
		 * 设置连接的RpcServer的IP地址和端口号
		 * 注意：该函数只负责设置，不负责连接
		 * @param host
		 * @param port
		 * @return
		 */
		public ProxyBuilder<T> connect(String host, int port) {
			this.host = host;
			this.port = port;
			return this;
		}
		
		
		/**
		 * 设置处理请求的线程数量
		 * @param threadCount
		 * @return
		 */
		public ProxyBuilder<T> setThreads(int threadCount) {
			this.threads = threadCount;
			return this;
		}
		
		
		/**
		 * 创建同步的代理。使用动态代理技术完成。
		 * 在同步代理中，线程会阻塞直到返回处理结果或者超时。
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public T build() {
			if(threads <= 0) {
				threads = Runtime.getRuntime().availableProcessors();
			}
			
			//创建rpcClient,连接远程rpcServer
			rpcClient = new RpcClient(host, port, timeoutMills, rpcInvokeHook, threads);
			//连接远程服务器
			rpcClient.connect();
			
			/**
			 * 这里是关键！
			 * 使用Proxy.newProxyInstance创建动态代理对象，需要三个参数:
			 * (1)类加载器; (2)接口的Class对象; (3)实现了InvocationHandler的触发管理器
			 * 注意，在动态代理中,动态代理和被代理者都要实现统一的接口！！！
			 * 这个接口中所定义的方法就是动态代理能够代理的那些方法！
			 * 所有动态代理对象需要向上转型为接口的类型。
			 */
			return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, rpcClient);
		}
		
		
		/**
		 * 创建异步代理。
		 * 在异步代理中，rpcFuture会被立即返回。
		 * @return
		 */
		public RpcClientAsyncProxy buildAsyncProxy() {
			if(threads <= 0)
				threads = Runtime.getRuntime().availableProcessors();
			
			rpcClient = new RpcClient(host, port, timeoutMills, rpcInvokeHook, threads);
			rpcClient.connect();
			
			//创建并返回异步代理对象
			return new RpcClientAsyncProxy(rpcClient, interfaceClass);
		}
	
	}//end of ProxyBuilder<T>
	
	
	/**
	 * 创建代理
	 * @param targetClass
	 * @return
	 */
	public static <T> ProxyBuilder<T> create(Class<T> interfaceClass) {
		return new ProxyBuilder<T>(interfaceClass);
	}
	
	
		
}








