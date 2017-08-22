package com.tao.rpc.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 信息输出类。
 * 负责输出信息。
 * 开启单线程，不断地检测阻塞队列，从头部取出info打印。
 * @author Tao
 *
 */

public class InfoPrinter {
	
	public static final boolean ACTIVE = true;
	
	private static final SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static BlockingQueue<String> printMissionQueue = new LinkedBlockingDeque<>();	//任务队列
	private static ExecutorService threadPool;
	
	
	static {
		//创建单线程的线程池，从任务队列中取出信息，输出
		threadPool = Executors.newSingleThreadExecutor();
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				
				try {
					//单线程一直运行
					while(true) {
						//从阻塞队列中取出一条信息
						String info = printMissionQueue.take();
						Date date = new Date();
						System.out.println("[" + DATA_FORMAT.format(date) + "]:" + info);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	public static void println(String info) {
		if(!ACTIVE) {
			return;
		}
		//向阻塞队列中添加一条将要打印的信息
		printMissionQueue.add(info);
	}
	
	
	public static void exit() {
		if(!ACTIVE) {
			return;
		}
		//关闭线程池
		threadPool.shutdownNow();
	}
	
	
}





