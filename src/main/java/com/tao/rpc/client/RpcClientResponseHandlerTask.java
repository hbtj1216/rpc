package com.tao.rpc.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

import com.tao.rpc.domain.RpcResponse;
import com.tao.rpc.future.RpcFuture;

/**
 * RpcClientResponseHandlerRunnable是一个实现了Runnable接口的类。
 * 每个RpcClientResponseHandlerRunnable对象占用一个线程。
 * 主要负责对每个RpcResponse进行处理并生成对应的RpcFuture。
 * @author Tao
 *
 */

public class RpcClientResponseHandlerTask implements Runnable {
	
	private BlockingQueue<RpcResponse> responseQueue;				//阻塞任务队列
	private ConcurrentMap<Integer, RpcFuture> invokeIdRpcFutureMap;	//RpcFuture注册表
	
	//构造函数
	public RpcClientResponseHandlerTask(
			BlockingQueue<RpcResponse> responseQueue,
			ConcurrentMap<Integer, RpcFuture> invokeIdRpcFutureMap) {
		
		//传入两个引用
		this.responseQueue = responseQueue;
		this.invokeIdRpcFutureMap = invokeIdRpcFutureMap;
	}
	
	
	//每个线程要跑的方法
	@Override
	public void run() {
		
		//循环运行,线程不终止
		while(true) {
			try {
				/**
				 * 注意这里的处理流程：
				 * 首先，从外部传入的invokeIdRpcFutureMap中已经注册有了id对应请求的RpcFuture对象。
				 * responseQueue是一个阻塞队列, 调用take()方法是直到队列中有RpcResponse对象时才返回。
				 * 这时, 我们直接处理对应的RpcResponse后生成RpcFuture对象。
				 * 特别注意, 当处理完一个RpcResponse之后, 需要从invokeIdRpcFutureMap中移除本次RpcResponse对应
				 * 的已经处理好的RpcFuture对象, 所以要调用remove()方法。
				 */
				//从阻塞队列的头部取出一个rpcResponse
				RpcResponse rpcResponse = responseQueue.take();
				
				int id = rpcResponse.getId();
				//需要移除对应的rpcFuture
				RpcFuture rpcFuture = invokeIdRpcFutureMap.remove(id);
				if(rpcResponse.isInvokeSuccess()) {
					//调用成功
					rpcFuture.setResult(rpcResponse.getResult());
				}
				else {
					//抛出了异常
					rpcFuture.setThrowable(rpcResponse.getThrowable());
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
