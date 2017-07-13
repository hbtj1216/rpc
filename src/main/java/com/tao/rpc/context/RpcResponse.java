package com.tao.rpc.context;

/**
 * rpc response 应答体
 * 包括: id, 结果, 异常, 调用成功的标志
 * @author Tao
 *
 */

public class RpcResponse {
	
	private int id;						//id
	private Object result;				//结果
	private Throwable throwable;		//异常
	private boolean isInvokeSuccess;	//调用成功的标记
	
	
	public RpcResponse(int id, Object resultOrThrowable, boolean isInvokeSuccess) {
		this.id = id;
		this.isInvokeSuccess = isInvokeSuccess;
		if(isInvokeSuccess) {
			//成功, 返回结果
			result = resultOrThrowable;
		}
		else {
			throwable = (Throwable) resultOrThrowable;
		}
	}


	public int getId() {
		return id;
	}


	public Object getResult() {
		return result;
	}


	public Throwable getThrowable() {
		return throwable;
	}


	public boolean isInvokeSuccess() {
		return isInvokeSuccess;
	}

}
