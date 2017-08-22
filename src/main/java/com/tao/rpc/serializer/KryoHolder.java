package com.tao.rpc.serializer;

import com.esotericsoftware.kryo.Kryo;

public class KryoHolder {

	private static ThreadLocal<Kryo> threadLocalKryo = new ThreadLocal<Kryo>() {
		protected Kryo initialValue() {
			Kryo kryo = new KryoReflectionFactory();

			return kryo;
		}
	};
	
	//获得调用线程的Kryo对象
	public static Kryo get() {
		return threadLocalKryo.get();
	}

}
