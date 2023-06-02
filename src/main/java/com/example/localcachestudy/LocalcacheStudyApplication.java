package com.example.localcachestudy;

import com.example.localcachestudy.cache.CacheItem;
import com.example.localcachestudy.cache.CacheStore;
import com.example.localcachestudy.cache.LoadingCache;
import com.example.localcachestudy.repository.CacheStoreRepository;
import com.example.localcachestudy.repository.LoadingCacheRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LocalcacheStudyApplication {
	private static final int THREADS_NUMBER = 100;
	private static final long TTL = 100L;
	private static List<String> keyList80 = new ArrayList<>();
	private static List<String> keyList20 = new ArrayList<>();
	private static final LoadingCacheRepository loadingCacheRepository = new LoadingCacheRepository();
	private static final CacheStoreRepository cacheStoreRepository = new CacheStoreRepository();
	private static final LoadingCache loadingCache = new LoadingCache(loadingCacheRepository, TTL);
	private static final CacheStore cacheStore = new CacheStore(cacheStoreRepository);
	public static void main(String[] args) {
		printHeapMemory();
		generateKeys();
		동시에_10000개_읽기();

	}

	private static void 동시에_10000개_읽기() {
		generateKeys();

		System.out.println("동시에 10000개 읽기");
		test(10000, new Runnable() {
			@Override
			public void run() {
				Random random = new Random();
				double randomValue = random.nextDouble();
				String key;
				if (randomValue < 0.2) {
					key = random.nextInt(keyList20.size()) + "";
				} else {
					key = random.nextInt(keyList80.size()) + "";
				}
				System.out.println(loadingCache.get(key));
			}
		});
	}

	private static void test(int iterationCount, Runnable task) {
		long startTime = System.currentTimeMillis();

		ExecutorService executorService = Executors.newFixedThreadPool(THREADS_NUMBER);
		for (int i = 0; i < iterationCount; i++) {
			executorService.submit(task);
		}

		executorService.shutdown();

		try {
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.err.println("쓰레드 풀 종료 중 예외가 발생했습니다: " + e.getMessage());
		}

		long endTime = System.currentTimeMillis();
		System.out.println("실행시간: " + (endTime - startTime) + "ms");

		printHeapMemory();
	}

	private static void generateKeys() {
		for (int i = 1; i <= 200; i++) {
			String key = i + "";
			keyList20.add(key);
			loadingCacheRepository.saveWithoutSleep(key, key);
			loadingCache.getCache().put(key, new CacheItem(key, TTL));
			System.out.println("key = " + key);;
		}
		for (int i = 201; i <= 1000; i++) {
			String key = i + "";
			keyList80.add(key);
			loadingCacheRepository.saveWithoutSleep(key, key);
			loadingCache.getCache().put(key, new CacheItem(key, TTL));
			System.out.println("key = " + key);;
		}
	}

	private static void printHeapMemory() {
		long heapSize = Runtime.getRuntime().totalMemory();
		long heapMaxSize = Runtime.getRuntime().maxMemory();
		long heapFreeSize = Runtime.getRuntime().freeMemory();

		System.out.println("현재 힙 메모리 사이즈: " + heapSize / (1024 * 1024) + "MB");
		System.out.println("최대 힙 메모리 사이즈: " + heapMaxSize / (1024 * 1024) + "MB");
		System.out.println("free 메모리 사이즈: " + heapFreeSize / (1024 * 1024) + "MB");
		System.out.println("===================");
	}
}
