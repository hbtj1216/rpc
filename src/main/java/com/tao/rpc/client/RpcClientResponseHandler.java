package com.tao.rpc.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import com.tao.rpc.domain.RpcResponse;
import com.tao.rpc.future.RpcFuture;

/**
 * RpcClient远程调用之后，获得服务器返回的RpcResponse。
 * 成功返回，对RpcResponse的处理交由RpcClientResponseHandler完成。
 * RpcClientResponseHandler中会启动一个线程池，执行的任务定义在RpcClientResponseHandlerRunnable中。
 * @author Tao
 *
 */

public class RpcClientResponseHandler {
	
	//创建一个LinkedBlockingDeque来保存所有的RpcResponse，做为任务队列。
	private BlockingQueue<RpcResponse> responseQueue = new LinkedBlockingDeque<>();
	//创建一个ConcurrentHashMap来保存每个RpcRequest对应的id和结果RpcFuture。
	private ConcurrentMap<Integer, RpcFuture> invokeIdRpcFutureMap = new ConcurrentHashMap<>();
	//线程池。
	private ExecutorService threadPool;	
	
	
	public RpcClientResponseHandler(int threads) {
		//在构造函数中初始化线程池,需要指定线程池中线程的数量
		//启动线程池
		this.threadPool = Executors.newFixedThreadPool(threads);
		for(int i = 0; i < threads; i++) {
			threadPool.execute(new RpcClientResponseHandlerRunnable(responseQueue, invokeIdRpcFutureMap));
		}
	}
	
	
	/**
	 * 注册RpcFuture
	 * @param id
	 * @param rpcFuture
	 */
	public void register(int id, RpcFuture rpcFuture) {
		invokeIdRpcFutureMap.put(id, rpcFuture);
	}
	
	
	/**
	 * 添加RpcResponse
	 * @param rpcResponse
	 */
	public void addResponse(RpcResponse rpcResponse) {
		responseQueue.add(rpcResponse);
	}
	
}






