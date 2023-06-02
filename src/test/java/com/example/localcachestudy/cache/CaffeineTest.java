package com.example.localcachestudy.cache;

import com.example.localcachestudy.repository.LoadingCacheRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.example.localcachestudy.cache.LoadingCacheTest.printHeapMemory;

public class CaffeineTest {
    private final Cache<String, String> caffeine = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .build();
    private static final LoadingCacheRepository loadingCacheRepository = new LoadingCacheRepository();
    private static final int THREADS_NUMBER = 10;
    private static final int TOTAL_KEYS = 1000;

    @Test
    public void 동시에_10000개_읽기() {
        fillCache();
        test(10000);
    }

    @Test
    public void 캐시_초기화_후_동시에_100개_읽기() {
        fillRepository();
        test(100);
    }

    @Test
    public void key의_50퍼센트_만료_후_동시에_1000개_읽기() {
        fillRepository();
        fillCache();
        for (int i = 100; i <= 600; i++) {
            caffeine.invalidate(i + "");
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
                    key = random.nextInt((int) (TOTAL_KEYS * 0.2)) + "";
                } else {
                    key = TOTAL_KEYS * 0.2 + random.nextInt((int) (TOTAL_KEYS * 0.8)) + "";
                }
                String result = caffeine.get(key, loadingCacheRepository::find);
                latch.countDown();
                System.out.println(Thread.currentThread().getName() + " : " + result + " : " + finalI);
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

    private void fillCache() {
        for (int i = 1; i <= 1000; i++) {
            String key = i + "";
            caffeine.put(key, key);
        }
    }

    private void fillRepository() {
        for (int i = 1; i <= 1000; i++) {
            String tmp = String.valueOf(i);
            loadingCacheRepository.saveWithoutSleep(tmp, tmp);
        }
    }

}
