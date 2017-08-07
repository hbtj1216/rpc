package com.tao.rpc.example.server.test;

import com.tao.rpc.aop.RpcInvokeHook;
import com.tao.rpc.example.server.TestInterface;
import com.tao.rpc.example.server.TestInterfaceImpl;
import com.tao.rpc.server.RpcServer;
import com.tao.rpc.server.RpcServerBuilder;
import com.tao.rpc.utils.InfoPrinter;

/**
 * Rpc的Server服务器。
 * 使用之前的封装好的类，创建一个真实的Server，等待客户端的连接。
 * @author Tao
 *
 */

public class Server {


	//钩子
	private static RpcInvokeHook hook;


	static {
	    hook = new RpcInvokeHook() {
            @Override
            public void beforeInvoke(String methodName, Object[] args) {
                InfoPrinter.println(methodName + " 被调用.");
            }

            @Override
            public void afterInvoke(String methodName, Object[] args) {
                InfoPrinter.println(methodName + " 调用完毕.");
            }
        };
    }

	
	/**
	 * 启动RPC服务器
	 */
	public void startServer() {
		
		//1) 创建服务器具体提供的类和方法(方法属于具体实现类)
		TestInterface serviceProvider = new TestInterfaceImpl();
		//2) 创建一个RpcServer实例
		//首先创建一个builder, 然后设置一些参数, 最后通过调用build()方法创建RpcServer实例。
		RpcServer rpcServer = RpcServerBuilder.create()
								.setServiceInterface(TestInterface.class)
								.setServiceProvider(serviceProvider)
                                .setHook(hook)
								.setThreads(4)
								.bind(4399)
								.build();
		//3) 启动rpcServer
		rpcServer.start();
		
	}
	
	
	
	//main
	public static void main(String[] args) {
		
		new Server().startServer();
		
	}
	
	
	
}








