package com.pcl.gitea.redis;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RedisMsgThreadExecutor {

	private  static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(8,16,100,TimeUnit.MINUTES,new ArrayBlockingQueue<Runnable>(2000));

	public static ThreadPoolExecutor getThreadPool() {
		return threadPool;
	}

	
}
