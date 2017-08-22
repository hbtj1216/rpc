package com.tao.rpc.future;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.tao.rpc.exception.RpcTimeoutException;

/**
 * 异步RPC的Future类
 * 注意:当state == STATE_AWAIT时候，RpcFuture对象获得result。
 * 当状态变为STATE_SUCCESS时候，从this.result返回结果过。
 * 
 * @author Tao
 *
 */

public class RpcFuture {
	
	public static final int STATE_AWAIT = 0;		//阻塞
	public static final int STATE_SUCCESS = 1;		//成功
	public static final int STATE_EXCEPTION = 2;	//异常
	
	private CountDownLatch countDownLatch;	
	private volatile int state;				//状态	
		
	private Object result;
	private Throwable throwable;
	private RpcFutureListener rpcFutureListener = null;
	
	
	public RpcFuture() {
		this.countDownLatch = new CountDownLatch(1);
		//RpcFuture对象被创建的时候，就讲状态(state)初始化为阻塞状态(STATE_AWAIT)
		this.state = STATE_AWAIT;
	}
	
	
	/**
	 * 获取结果
	 * @return
	 * @throws Throwable
	 */
	public Object get() throws Throwable {
		countDownLatch.await();	//调用该对象的get()方法的当前线程会阻塞在此处
		if(state == STATE_SUCCESS) {
			return result;
		}
		else if(state == STATE_EXCEPTION) {
			//发生异常
			throw throwable;
		}
		else {
			throw new RuntimeException("RpcFuture Exception!");
		}
	}
	
	
	public Object get(long timeoutMills) throws Throwable {
		boolean awaitSuccess;
		//调用线程会阻塞，直到超时或者中断
		awaitSuccess = countDownLatch.await(timeoutMills, TimeUnit.MILLISECONDS);	//单位(毫秒)
		//超时或者中断，返回false
		if(!awaitSuccess) {
			throw new RpcTimeoutException();
		}
		
		if(state == STATE_SUCCESS) {
			return result;
		}
		else if(state == STATE_EXCEPTION) {
			//发生异常
			throw throwable;
		}
		else {
			throw new RuntimeException("RpcFuture Exception!");
		}
	}
	
	
	/**
	 * 成功获得result,并设置this.result
	 * @param result
	 */
	public synchronized void setResult(Object result) {
		if(state != STATE_AWAIT) {
			throw new IllegalStateException("can not set result to a RpcFuture instance "
					+ "which has already get result or throwable!");
		}
		//state == STATE_AWAIT
		this.result = result;	//设置result
		state = STATE_SUCCESS;	//更改state
		
		if(rpcFutureListener != null) {
			rpcFutureListener.onResult(result);
		}
		
		//计数器值-1,唤醒等待的线程来获取rpcFuture中的结果
		countDownLatch.countDown();
	}
	
	
	/**
	 * 发生异常，进行设置
	 * @param throwable
	 */
	public synchronized void setThrowable(Throwable throwable) {
		if(state != STATE_AWAIT) {
			throw new IllegalStateException("can not set throwable to a RpcFuture instance which has already get result " +
                    "or throwable!");
		}
		//state == STATE_AWAIT
		this.throwable = throwable;
		state = STATE_EXCEPTION;
		
		if(rpcFutureListener != null) {
			rpcFutureListener.onException(throwable);
		}
		
		//计数器值-1,唤醒等待的线程
		countDownLatch.countDown();
	}
	
	
	/**
	 * 判断阻塞在rocFuture上的过程是否完成
	 * @return
	 */
	public boolean isDone() {
		return state != STATE_AWAIT;
	}
	
	
	/**
	 * 设置RpcFuture对象的RpcFutureListener
	 * @param rpcFutureListener
	 */
	public synchronized void setRpcFutureListener(RpcFutureListener rpcFutureListener) {
		if(state != STATE_AWAIT) {
			throw new RuntimeException("unable to set listener to a RpcFuture which is done.");
		}
		//state == STATE_AWAIT
		this.rpcFutureListener = rpcFutureListener;
	}
	
	
}






















