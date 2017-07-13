package com.tao.rpc.example.server;

import java.util.Arrays;
import java.util.List;

import com.tao.rpc.example.CustomException;
import com.tao.rpc.example.CustomObject;

/**
 * TestInterface接口的具体实现类TestInterfaceImpl。
 * 这也是真实的 被代理者。
 * 存在于服务器端。
 * @author Tao
 *
 */

public class TestInterfaceImpl implements TestInterface {

	@Override
	public String methodWithoutArg() {
		
		return "methodWithoutArg() 运行完毕, 正常返回!";
	}

	@Override
	public String methodWithArgs(String arg1, String arg2) {
		
		return "methodWithArgs(" + arg1 + ", " + arg2 + ") "
				+ "运行完毕, 正常返回! 参数分别为: " + arg1 + ", " + arg2;
	}

	@Override
	public CustomObject methodWithCustomObject(CustomObject customObject) {
		
		CustomObject updateObject = new CustomObject(customObject.getName() + " update", customObject.getAge() + 20);
		//返回更新之后的CustomObject对象
		return updateObject;
	}

	@Override
	public List<String> methodReturnList(String arg1, String arg2) {
		
		return Arrays.asList(arg1, arg2);
	}

	@Override
	public void methodThrowException() {

		throw new CustomException();
	}

	@Override
	public void methodTimeout() {
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void methodReturnVoid() {
		
		return;
	}

	@Override
	public String methodDelayOneSecond() {

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "方法延迟1s, 现在正常返回!";
	}

	@Override
	public int methodForMultiThread(int threads) {

		return threads;
	}

	@Override
	public String methodForPerformance() {

		return "Tony is dancing...";
	}

}
