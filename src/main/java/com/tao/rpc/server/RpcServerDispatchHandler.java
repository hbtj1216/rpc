package com.tao.rpc.server;

import com.tao.rpc.domain.RpcRequest;
import com.tao.rpc.domain.RpcRequestWrapper;
import com.tao.rpc.utils.InfoPrinter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * RpcServer客户端的业务处理类。
 * 每一个连接的建立对应产生一个RpcServerDispatchHandler对象，占用一个线程。
 * @author tao
 *
 */

public class RpcServerDispatchHandler extends ChannelInboundHandlerAdapter {
	
	//RpcServerRequestHandler对象引用
	private RpcServerRequestHandler rpcServerRequestHandler;
	
	//构造函数
	public RpcServerDispatchHandler(RpcServerRequestHandler rpcServerRequestHandler) {
		
		this.rpcServerRequestHandler = rpcServerRequestHandler;
	}

	
	/**
	 * 当本线程对应的RpcServerDispatchHandler收到客户端发来的消息时，触发
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		
		//获得rpcRequest
		RpcRequest rpcRequest = (RpcRequest) msg;
		//将rpcRequest和对应的channel包装起来
		RpcRequestWrapper rpcRequestWrapper = new RpcRequestWrapper(rpcRequest, ctx.channel());
		
		//交给rpcServerRequestHandler中的线程池进行处理
		rpcServerRequestHandler.addRequest(rpcRequestWrapper);
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		
		
	}


	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		
		InfoPrinter.println("一个新的客户端连接到本服务器!");
	}


	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		InfoPrinter.println("客户端连接关闭!");
	}
	
	
	
	
	
}








