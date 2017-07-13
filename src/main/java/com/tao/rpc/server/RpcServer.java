package com.tao.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.tao.rpc.aop.RpcInvokeHook;
import com.tao.rpc.netty.NettyKryoDecoder;
import com.tao.rpc.netty.NettyKryoEncoder;
import com.tao.rpc.utils.InfoPrinter;

/**
 * RPC远程调用的Server端
 * @author tao
 *
 */

public class RpcServer {
	
	private Class<?> interfaceClass;	//Class对象的引用
	private Object serviceProvider;		//服务提供者(具体实现了接口的类)
	
	private int port;		//服务器端端口号
	private int threads;	//服务器端处理请求的线程池中的线程数
	private RpcInvokeHook rpcInvokeHook;	//钩子
	
	//RpcRequest的处理者
	private RpcServerRequestHandler rpcServerRequestHandler; 
	
	
	//构造函数
	public RpcServer(
			Class<?> interfaceClass,
			Object serviceProvider,
			int port,
			int threads,
			RpcInvokeHook rpcInvokeHook
				) {

		this.interfaceClass = interfaceClass;
		this.serviceProvider = serviceProvider;
		this.port = port;
		this.threads = threads;
		this.rpcInvokeHook = rpcInvokeHook;
		
		//创建RpcServerRequestHandler对象，开启新的线程
		rpcServerRequestHandler = new RpcServerRequestHandler(this.interfaceClass, 
				this.serviceProvider, this.threads, this.rpcInvokeHook);
		
		rpcServerRequestHandler.start();
	}
	
	
	/**
	 * 启动RpcServer
	 */
	public void start() {
		//bossGroup占用一个线程，主要负责accept每个连接请求
		//每个连接请求被分配一个workerGroup进行处理
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			//服务器端的启动器
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, bossGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							
							ChannelPipeline pipeline = ch.pipeline();
							
							pipeline.addLast("Decoder", new NettyKryoDecoder());	//解码器
							pipeline.addLast("RpcServerDispacherHandler", 
									new RpcServerDispatchHandler(rpcServerRequestHandler));
							pipeline.addLast("Encoder", new NettyKryoEncoder());
						}
					});
			bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		    bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
		    bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		    
		    //绑定端口, 开启监听
		    ChannelFuture  channelFuture = bootstrap.bind(port).sync();
			
		    Channel channel = channelFuture.channel();
		    InfoPrinter.println("RpcServer 已经启动!");
		    InfoPrinter.println(printInfo());
		    InfoPrinter.println(interfaceClass.getSimpleName() + " 准备就绪, 正在提供服务!");
		    
		    channel.closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	
	/**
	 * 终止RpcServer
	 */
	public void stop() {
		System.out.println("server stop success!");
	}
	
	
	/**
	 * 打印服务器信息
	 * @return
	 */
	public String printInfo() {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			String ip = addr.getHostAddress();
			int port = this.port;
			return "服务器IP: " + ip + "   " + "端口号: " + port;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
}















