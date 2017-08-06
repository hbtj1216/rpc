package com.tao.rpc.client;

import com.tao.rpc.domain.RpcResponse;
import com.tao.rpc.utils.InfoPrinter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * RpcClient客户端的业务处理类。
 * 每一个连接的建立对应产生一个RpcClientDispatchHandler对象，占用一个线程。
 * @author Tao
 *
 */

public class RpcClientDispatchHandler extends ChannelInboundHandlerAdapter {
	
	//客户端的RpcResponse处理对象的引用
	private RpcClientResponseHandler rpcClientResponseHandler;
	//连接断开的监听者
	private RpcClientChannelInactiveListener rpcClientChannelInactiveListener = null;
	
	
	//构造函数
	public RpcClientDispatchHandler(
			RpcClientResponseHandler rpcClientResponseHandler,
			RpcClientChannelInactiveListener rpcClientChannelInactiveListener) {
		
		this.rpcClientResponseHandler = rpcClientResponseHandler;
		this.rpcClientChannelInactiveListener = rpcClientChannelInactiveListener;
	}
	
	
	/**
	 * 当连接断开, 触发
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		
		if(rpcClientChannelInactiveListener != null) {
			rpcClientChannelInactiveListener.onInactive();
		}
	}
	
	
	/**
	 * 当从服务器接收到信息, 触发
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		
		//获得返回的RpcResponse对象
		RpcResponse rpcResponse = (RpcResponse) msg;
		//交由RpcClientResponseHandler对象进行处理
		rpcClientResponseHandler.addResponse(rpcResponse);
	}
	
	
	/**
	 * 发生异常, 触发
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		
		InfoPrinter.println("客户端发生异常!");
		cause.printStackTrace();
	}

	
	

}
