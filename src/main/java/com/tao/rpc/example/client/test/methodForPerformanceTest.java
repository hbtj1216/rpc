package com.tao.rpc.example.client.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.tao.rpc.client.RpcClientProxyBuilder;
import com.tao.rpc.example.client.TestInterface;
import com.tao.rpc.utils.InfoPrinter;

/**
 * 对方法methodForPerformance()进行测试
 * 
 * @author Tao
 *
 */

public class methodForPerformanceTest {

	private static TestInterface syncProxy;		//同步代理
	private static final int THREADS = 16; 		// 线程数
	private static final int INVOKES = 10000; 	// 每个线程调用的次数
	private static final int TIMEOUT = 300;
	private static final String HOST = "127.0.0.1";
	private static final int PORT = 4399;
	private static final int TEST_COUNT = 10;
	

	public static void main(String[] args) throws Exception {

		// 1.创建同步的客户端代理
		syncProxy = RpcClientProxyBuilder.create(TestInterface.class)
						.setTimeoutMills(TIMEOUT)
						.setThreads(THREADS)
						.connect(HOST, PORT)
						.build();

		
		// 2.创建线程池
		ExecutorService threadPool = Executors.newFixedThreadPool(THREADS);
		
		//进行多次测试
		for (int n = 0; n < TEST_COUNT; n++) {
			
			InfoPrinter.println("===========【第 " + (n+1) + " 次测试开始】============");
			CountDownLatch countDownLatch = new CountDownLatch(THREADS * INVOKES);
			InfoPrinter.println("methodForPerformance() test started.");

			long startTime = System.currentTimeMillis(); // 开始时间

			for (int i = 0; i < THREADS; i++) {
				// 启动线程
				threadPool.execute(new MethodForPerformanceRunnable(syncProxy, countDownLatch));
			}

			// 主线程等待所有子线程完成
			if (countDownLatch.await(TIMEOUT, TimeUnit.SECONDS)) {
				long endTime = System.currentTimeMillis(); // 结束时间
				// 计算系统吞吐量(每秒事务数量)
				double deltaTime = (endTime - startTime) / 1000f;
				double tps = (THREADS * INVOKES) / deltaTime;
				InfoPrinter.println("methodForPerformance() test finished.");
				InfoPrinter.println("耗时: " + deltaTime);
				InfoPrinter.println("系统吞吐量(TPS): " + tps);
			} else {
				InfoPrinter.println("methodForPerformance() test failed.");
			}
			InfoPrinter.println("===========【第 " + (n+1) + " 次测试结束】============");
			Thread.sleep(1000);

		}

	}

	private static class MethodForPerformanceRunnable implements Runnable {

		private TestInterface syncProxy; // 代理
		private CountDownLatch countDownLatch;

		public MethodForPerformanceRunnable(TestInterface syncProxy, CountDownLatch countDownLatch) {

			this.syncProxy = syncProxy;
			this.countDownLatch = countDownLatch;
		}

		@Override
		public void run() {
			// 每个线程要跑的方法
			for (int i = 0; i < INVOKES; i++) {
				// INVOKES次调用
				this.syncProxy.methodForPerformance();
				countDownLatch.countDown();
			}

		}

	}

}
