package com.example.localcachestudy.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CaffeineCacheTest {

    private static final int TOTAL_KEYS = 1000;
    private static final int READ_THREADS = 10000;
    private static final int READ_BATCH_SIZE = 100;
    private static final int EXPIRED_KEYS_1 = 100;
    private static final int EXPIRED_KEYS_2 = 400;

    private final Cache<String, Integer> cache;

    public CaffeineCacheTest() {
        cache = Caffeine.newBuilder()
                .maximumSize(TOTAL_KEYS)
                .expireAfterAccess(1, TimeUnit.SECONDS)
                .build();
    }

    public void initializeCache() {
        for (int i = 1; i <= TOTAL_KEYS; i++) {
            cache.put(String.valueOf(i), i);
        }
    }

    public void concurrentReadsFromFullCache() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(READ_THREADS);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < READ_THREADS; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < READ_BATCH_SIZE; j++) {
                    int key = (int) (Math.random() * TOTAL_KEYS) + 1;
                    cache.getIfPresent(String.valueOf(key));
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        System.out.println("Concurrent Reads from Full Cache: " + (endTime - startTime) + "ms");
    }

    public void concurrentReadsFromEmptyCache() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(READ_THREADS);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < READ_THREADS; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < READ_BATCH_SIZE; j++) {
                    int key = (int) (Math.random() * TOTAL_KEYS) + 1;
                    cache.getIfPresent(String.valueOf(key));
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        System.out.println("Concurrent Reads from Empty Cache: " + (endTime - startTime) + "ms");
    }

    public void concurrentReadsWithExpiredKeys() throws InterruptedException {
        // Mark some keys as expired
        for (int i = 1; i <= EXPIRED_KEYS_1; i++) {
            cache.put(String.valueOf(i), i);
        }
        for (int i = TOTAL_KEYS - EXPIRED_KEYS_2 + 1; i <= TOTAL_KEYS; i++) {
            cache.put(String.valueOf(i), i);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(READ_THREADS);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < READ_THREADS; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < READ_BATCH_SIZE; j++) {
                    int key = (int) (Math.random() * TOTAL_KEYS) + 1;
                    cache.getIfPresent(String.valueOf(key));
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        System.out.println("Concurrent Reads with Expired Keys: " + (endTime - startTime) + "ms");
    }

    public static void main(String[] args) throws InterruptedException {
        CaffeineCacheTest test = new CaffeineCacheTest();
        test.initializeCache();
        test.concurrentReadsFromFullCache();
        test.concurrentReadsFromEmptyCache();
        test.concurrentReadsWithExpiredKeys();
    }
}

