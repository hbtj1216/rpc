package com.tao.rpc.example.client.test;


import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.MidiDevice.Info;

import com.tao.rpc.aop.RpcInvokeHook;
import com.tao.rpc.client.RpcClientAsyncProxy;
import com.tao.rpc.client.RpcClientProxyBuilder;
import com.tao.rpc.example.CustomObject;
import com.tao.rpc.example.client.TestInterface;
import com.tao.rpc.future.RpcFuture;
import com.tao.rpc.future.RpcFutureListener;
import com.tao.rpc.utils.InfoPrinter;

/**
 * 对普通方法的测试
 * 包括：
 * 	public String methodWithoutArg();
	public String methodWithArgs(String arg1, String arg2);
	public CustomObject methodWithCustomObject(CustomObject customObject);
	public List<String> methodReturnList(String arg1, String arg2);
	public void methodThrowException();
	public void methodTimeout();
	public void methodReturnVoid();
	public String methodDelayOneSecond();
 * @author Tao
 *
 */

public class FunctionsTest {
	
	private static TestInterface 		syncProxy;		//同步代理
	private static RpcClientAsyncProxy 	asyncProxy;		//异步代理
	
	private static final int THREADS = 16; 		// 线程数
	private static final int TIMEOUT = 3000;
	private static final String HOST = "127.0.0.1";
	private static final int PORT = 4399;
	private static RpcInvokeHook hook;
	
	
	static {	
		//初始化
		hook = new RpcInvokeHook() {
			
			@Override
			public void beforeInvoke(String methodName, Object[] args) {
				InfoPrinter.println(methodName + " 方法远程调用开始...");
			}
			
			@Override
			public void afterInvoke(String methodName, Object[] args) {
				InfoPrinter.println(methodName + " 方法远程调用结束。");
			}
		};
			
	}
	
	
	/**
	 * 使用同步代理测试所有方法
	 * @throws Exception 
	 */
	public void syncTest() throws Exception {
		
		//创建同步代理
		syncProxy = RpcClientProxyBuilder.create(TestInterface.class)
									.setTimeoutMills(TIMEOUT)
									.setHook(hook)
									.setThreads(THREADS)
									.connect(HOST, PORT)
									.build();

		Thread.sleep(2000);
		System.out.print("\n");
		InfoPrinter.println("=============================");
		String result1 = syncProxy.methodWithoutArg();
		InfoPrinter.println("返回值(String类型): " + result1);
		InfoPrinter.println("=============================");
		
		Thread.sleep(2000);
		System.out.print("\n");
		InfoPrinter.println("=============================");
		String result2 = syncProxy.methodWithArgs("第1个参数", "第2个参数");
		InfoPrinter.println("返回值(String类型): " + result2);
		InfoPrinter.println("=============================");
		
		Thread.sleep(2000);
		System.out.print("\n");
		InfoPrinter.println("=============================");
		CustomObject objOld = new CustomObject("Tom", 25);
		CustomObject objNew = syncProxy.methodWithCustomObject(objOld);
		InfoPrinter.println("返回值(CustomObject): " + objNew);
		InfoPrinter.println("=============================");
		
		Thread.sleep(2000);
		System.out.print("\n");
		InfoPrinter.println("=============================");
		List<String> resultList = syncProxy.methodReturnList("第1参数", "第2个参数");
		InfoPrinter.println("返回值(List<String>): " + resultList);
		InfoPrinter.println("=============================");
		
		/*Thread.sleep(2000);
		System.out.print("\n");
		InfoPrinter.println("=============================");
		syncProxy.methodThrowException();
		InfoPrinter.println("=============================");*/
		
		/*Thread.sleep(2000);
		System.out.print("\n");
		InfoPrinter.println("=============================");
		syncProxy.methodTimeout();
		InfoPrinter.println("=============================");*/
		
		Thread.sleep(2000);
		System.out.print("\n");
		InfoPrinter.println("=============================");
		syncProxy.methodReturnVoid();
		InfoPrinter.println("=============================");
		
		Thread.sleep(2000);
		System.out.print("\n");
		InfoPrinter.println("=============================");
		String result = syncProxy.methodDelayOneSecond();
		InfoPrinter.println("返回值(List<String>): " + result);
		InfoPrinter.println("=============================");
		
	}
	
	
	/**
	 * 使用异步代理测试所有方法 TODO
	 * @throws InterruptedException 
	 */
	public void asyncTest() throws Exception {
		
		//创建异步代理
		asyncProxy = RpcClientProxyBuilder.create(TestInterface.class)
									.setTimeoutMills(TIMEOUT)
									.setHook(hook)
									.setThreads(THREADS)
									.connect(HOST, PORT)
									.buildAsyncProxy();
		
		final CountDownLatch countDownLatch = new CountDownLatch(1);
		
		//异步调用
		RpcFuture rpcFuture = asyncProxy.call("methodDelayOneSecond");
		while(rpcFuture == null) {
			Thread.sleep(200);
		}
		//设置监听器
		rpcFuture.setRpcFutureListener(new RpcFutureListener() {
			
			@Override
			public void onResult(Object result) {
				countDownLatch.countDown();
			}
			
			@Override
			public void onException(Throwable throwable) {
				InfoPrinter.println(throwable + " 异常被捕获.");
			}
		});
		
		if(!countDownLatch.await(2000, TimeUnit.MILLISECONDS)) {
			//超时
			InfoPrinter.println("没能监听到返回的rpcFuture");
		}
		
		try {
			InfoPrinter.println("rpcFuture的内容: " + (String) rpcFuture.get());
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}
	
	
	//main
	public static void main(String[] args) throws Exception {
			
		FunctionsTest test = new FunctionsTest();
		
		/*InfoPrinter.println("同步代理测试开始...");
		test.syncTest();
		InfoPrinter.println("同步代理测试结束...");*/
		
		InfoPrinter.println("异步代理测试开始...");
		test.asyncTest();
		InfoPrinter.println("异步代理测试结束...");
			
	}
	
}





