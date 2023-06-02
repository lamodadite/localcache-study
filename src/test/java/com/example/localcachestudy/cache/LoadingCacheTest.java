package com.example.localcachestudy.cache;

import com.example.localcachestudy.repository.CacheStoreRepository;
import com.example.localcachestudy.repository.LoadingCacheRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class LoadingCacheTest {
    private static final int THREADS_NUMBER = 10;
    private static final int TOTAL_KEYS = 1000;
    private static final long TTL = 10000000L;
    private static List<String> keyList;
    private static final LoadingCacheRepository loadingCacheRepository = new LoadingCacheRepository();
    private static final LoadingCache loadingCache = new LoadingCache(loadingCacheRepository, TTL);

    @Test
    public void 동시에_10000개_읽기() {
        generateKeyList();
        fillCache();
        test(10000);
    }

    @Test
    public void 캐시_초기화_후_동시에_100개_읽기() {
        fillRepository();
        generateKeyList();
        test(100);
    }

    @Test
    public void key의_50퍼센트_만료_후_동시에_1000개_읽기() {
        fillRepository();
        generateKeyList();
        for (int i = 1; i <= 1000; i++) {
            if (i > 100 && i <= 600) {
                loadingCache.getCache().put(i + "", new CacheItem(i + "", 0));
                continue;
            }
            loadingCache.getCache().put(i + "", new CacheItem(i + "", TTL));
        }
        test(1000);
    }

    private void test(int bound) {
        long startTime = System.currentTimeMillis();
        ExecutorService service = Executors.newFixedThreadPool(THREADS_NUMBER);
        CountDownLatch latch = new CountDownLatch(bound);
        Random random = new Random();
        int key80percent = (int) (bound * 0.8);

        for (int i = 1; i <= bound; i++) {
            int finalI = i;
            service.submit(() -> {
                String key;
                if (finalI < key80percent) {
                    key = keyList.get(random.nextInt((int) (TOTAL_KEYS * 0.2)));
                } else {
                    key = keyList.get((int) (TOTAL_KEYS * 0.2 + random.nextInt((int) (TOTAL_KEYS * 0.8))));
                }
                String result = loadingCache.get(key);
                latch.countDown();
//                System.out.println(Thread.currentThread().getName() + " : " + result + " : " + finalI);
            });
        }

        service.shutdown();
        try {
            latch.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("실행시간: " + (endTime - startTime) + "ms");
        printHeapMemory();
    }

    private void generateKeyList() {
        keyList = new ArrayList<>();

        for (int i = 1; i <= 1000; i++) {
            keyList.add(String.valueOf(i));
        }
    }

    private void fillCache() {
        for (int i = 1; i <= 1000; i++) {
            loadingCache.getCache().put(i + "", new CacheItem(i + "", TTL));
        }
    }

    private void fillRepository() {
        for (int i = 1; i <= 1000; i++) {
            String tmp = String.valueOf(i);
            loadingCacheRepository.saveWithoutSleep(tmp, tmp);
        }
    }

    public static void printHeapMemory() {
        long heapSize = Runtime.getRuntime().totalMemory();
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        long heapFreeSize = Runtime.getRuntime().freeMemory();

        System.out.println("===================");
        System.out.println("현재 힙 메모리 사이즈: " + heapSize / (1024 * 1024) + "MB");
        System.out.println("최대 힙 메모리 사이즈: " + heapMaxSize / (1024 * 1024) + "MB");
        System.out.println("free 메모리 사이즈: " + heapFreeSize / (1024 * 1024) + "MB");
        System.out.println("===================");
    }

}