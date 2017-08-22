package com.tao.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import com.tao.rpc.aop.RpcInvokeHook;
import com.tao.rpc.domain.RpcRequest;
import com.tao.rpc.future.RpcFuture;
import com.tao.rpc.netty.NettyKryoDecoder;
import com.tao.rpc.netty.NettyKryoEncoder;
import com.tao.rpc.utils.InfoPrinter;


/**
 * RpcClient客户端类
 * 使用动态代理
 * RpcClient实现InvocationHandler接口，因此RpcClient本身是一个【触发管理器】
 * @author Tao
 *
 */

public class RpcClient implements InvocationHandler {
	
	//================================================================
	
	private String host;							//主机(IP地址)
	private int port;								//端口号
	private long timeoutMills = 0;					//超时时间(毫秒)
	private RpcInvokeHook rpcInvokeHook = null;		//AOP钩子
	
	//rpc调用返回的RpcResponse的处理对象
	private RpcClientDispatcherHandler rpcClientDispatcherHandler;
	
	//AtomicInteger对象，用于为每个rpc调用生成唯一的id
	private AtomicInteger invokeIdGenerator = new AtomicInteger(0);
	
	//Netty的启动器
	private Bootstrap bootstrap;
	
	//每一个RpcClient对应一个Channel
	private Channel channel;
	
	//断开连接的监听器
	private RpcClientChannelInactiveListener rpcClientChannelInactiveListener;
	//================================================================
	
	

	/**
	 * 构造函数
	 * @param host 	主机ip
	 * @param port	端口号
	 * @param timeoutMills	超时时间(毫秒)
	 * @param rpcInvokeHook	钩子
	 * @param threads	线程数量
	 */
	public RpcClient(String host, int port, long timeoutMills, RpcInvokeHook rpcInvokeHook, int threads) {
		
		this.host = host;
		this.port = port;
		this.timeoutMills = timeoutMills;	//异步等待的超时时间
		this.rpcInvokeHook = rpcInvokeHook;
		
		//处理RpcResponse的对象
		rpcClientDispatcherHandler = new RpcClientDispatcherHandler(threads);
		
		//创建监听连接断开的监听者对象
		rpcClientChannelInactiveListener = new RpcClientChannelInactiveListener() {
			
			//当连接断开，尝试重新连接
			@Override
			public void onInactive() {
				InfoPrinter.println("connection with server is closed.");
				InfoPrinter.println("try to reconnect to the server.");
				channel = null;
				do
	            {
	            	channel = tryConnect();
	            }
	            while(channel == null);	
			}
		};
	}
	
	
	
	/**
	 * RpcClient连接远程服务器, 使用Netty框架
	 */
	public void connect() {
		
		//启动器
		bootstrap = new Bootstrap();
		EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
		try {
			//1、初始化，配置
			bootstrap.group(eventLoopGroup)
					.channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<Channel>() {

						@Override
						protected void initChannel(Channel ch) throws Exception {
							
							ChannelPipeline pipeline = ch.pipeline();
							
							pipeline.addLast("Decoder", new NettyKryoDecoder());	//解码器
							pipeline.addLast("RpcClientResponseHandler",
									new RpcClientResponseHandler(rpcClientDispatcherHandler,
											rpcClientChannelInactiveListener));
							pipeline.addLast("Encoder", new NettyKryoEncoder());	//编码器
						}
					});
			bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			
            //2、连接服务器(支持断线重连)
            do {
				this.channel = tryConnect();
			} while (this.channel == null);
            
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * 尝试连接服务器。
	 * 成功, 返回对应的Channel; 失败, 返回null
	 * 
	 * @return
	 */
	private Channel tryConnect() {
		try {

			InfoPrinter.println("Try to connect to [" + this.host + ":" + this.port + "]...");
			//使用Netty框架里的bootstrap发起连接
			ChannelFuture future = bootstrap.connect(host, port).sync();	//同步等待
			if(future.isSuccess()) {
				//连接成功，返回对应的Channel
				InfoPrinter.println("Connect to [" + this.host + ":" + this.port + "] successed.");
				return future.channel();
			}
			else {
				//连接服务器失败
				InfoPrinter.println("Connect to [" + this.host + ":" + this.port + "] failed.");
				InfoPrinter.println("Try to reconnect in 5s. please wait a moment.");
				//等待5s
				Thread.sleep(5000);
				return null;	//返回null，在外部的while循环中可以控制是否进行重连
			}

		} catch (Exception e) {
			InfoPrinter.println("Connect to [" + this.host + ":" + this.port + "] failed.");
			InfoPrinter.println("Try to reconnect in 5s. please wait a moment.");
			try 
			{
				Thread.sleep(5000);
			} 
			catch (InterruptedException e1) 
			{
				e1.printStackTrace();
			}
			return null;
		}
	}
	
	
	
	/**
	 * 触发管理器(RpcClient)的invoke函数。
	 * 任何对 动态代理 的方法调用，都会转发到这里进行处理。
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		//方法调用之前添加的操作
		if(rpcInvokeHook != null) {
			rpcInvokeHook.beforeInvoke(method.getName(), args);
		}
		
		//方法的调用，通过远程请求服务器进行处理。
		RpcFuture rpcFuture = call(method.getName(), args);
		if(rpcFuture == null) {
			//连接不存在
			InfoPrinter.println("Disconnect with server.");
			return null;
		}
		
		//注意：同步 远程调用体现在这里!!
		//如果连接存在
		Object result;
		if(timeoutMills == 0) {
			//一直 阻塞 直到(有一个线程调用了rpcFuture.setResult(Object obj)，唤醒了当前线程)结果返回
			result = rpcFuture.get();
		}
		else {
			//阻塞 直到返回结果或者超时
			result = rpcFuture.get(timeoutMills);
		}
		
		//方法调用之后添加的操作
		if(rpcInvokeHook != null) {
			rpcInvokeHook.afterInvoke(method.getName(), args);
		}
		
		//返回服务器返回到客户端的结果
		//result只有两种情况：(1)正确的结果; (2)异常
		return result;
	}
	
	
	/**
	 * 函数调用
	 * @param methodName
	 * @param args
	 * @return
	 */
	public RpcFuture call(String methodName, Object[] args) {
			
		//1、首先需要在客户端本地注册RpcFuture
		RpcFuture rpcFuture = new RpcFuture();
		int id = invokeIdGenerator.addAndGet(1);	    //分配id
		rpcClientDispatcherHandler.register(id, rpcFuture);	//先注册RpcFuture
		
		//2、生成请求体，并发送给服务器
		RpcRequest rpcRequest = new RpcRequest(id, methodName, args);
		if(this.channel != null) {
			//如果连接存在,则直接发送出去
			this.channel.writeAndFlush(rpcRequest);
		}
		else {
			//连接不存在
			//注意：这里null只代表连接不存在，并不代表rpcFuture没有得到结果。
			return null;
		}
		
		//注意：这里可以直接返回这个创建的RpcFuture对象，不用操心它到底有没有获得服务器返回的结果。
		//因为在之后的操作(rpcFuture.get()),它会阻塞去拿结果的调用线程，所以能够保证直到结果返回了，才能拿到结果。
		return rpcFuture;	
	}


	//getter

	public String getHost() {
		return host;
	}



	public int getPort() {
		return port;
	}



	public long getTimeoutMills() {
		return timeoutMills;
	}



	public RpcInvokeHook getRpcInvokeHook() {
		return rpcInvokeHook;
	}



	public RpcClientDispatcherHandler getRpcClientDispatcherHandler() {
		return rpcClientDispatcherHandler;
	}



	public AtomicInteger getInvokeIdGenerator() {
		return invokeIdGenerator;
	}



	public Bootstrap getBootstrap() {
		return bootstrap;
	}



	public Channel getChannel() {
		return channel;
	}



	public RpcClientChannelInactiveListener getRpcClientChannelInactiveListener() {
		return rpcClientChannelInactiveListener;
	}
	
	
}









