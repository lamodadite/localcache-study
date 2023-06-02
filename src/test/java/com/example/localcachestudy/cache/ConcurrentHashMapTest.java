package com.example.localcachestudy.cache;

import com.example.localcachestudy.repository.CacheStoreRepository;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.localcachestudy.cache.LoadingCacheTest.printHeapMemory;

public class ConcurrentHashMapTest {
    private static final int THREADS_NUMBER = 10;
    private static final int TOTAL_KEYS = 1000;
    private static final ConcurrentHashMap<String, Integer> concurrentHashMap = new ConcurrentHashMap<>();

    @Test
    void 특정key에_동시에_100개_감소() {
        fillCacheStore();
        shrinkTest("1", 100);
    }

    @Test
    void 특정key에_동시에_101개_감소() {
        fillCacheStore();
        shrinkTest("1", 101);
    }

    @Test
    void 동시에_1000개_읽기와_동시에_100개_감소() {
        for (int i = 1; i <= TOTAL_KEYS; i++) {
            concurrentHashMap.put(i + "", i);
        }
        readAndShrinkTest();
    }

    private void shrinkTest(String key, int bound) {
        long startTime = System.currentTimeMillis();
        ExecutorService service = Executors.newFixedThreadPool(THREADS_NUMBER);
        CountDownLatch latch = new CountDownLatch(bound);

        for (int i = 0; i < bound; i++) {
            service.submit(() -> {
                int value = concurrentHashMap.put(key, concurrentHashMap.getOrDefault(key, 100) - 1);
                System.out.println(value);
                latch.countDown();
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
        System.out.println(concurrentHashMap.get(key));
    }

    private void readAndShrinkTest() {
        long startTime = System.currentTimeMillis();
        ExecutorService service = Executors.newFixedThreadPool(THREADS_NUMBER);
        CountDownLatch latch = new CountDownLatch(10100);
        Random random = new Random();

        for (int i = 0; i < 10000; i++) {
            service.submit(() -> {
                int value = concurrentHashMap.get(random.nextInt(TOTAL_KEYS) + 1 + "");
                System.out.println(value);
                latch.countDown();
            });
        }
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            service.submit(() -> {
                String tmp = random.nextInt(1000) + 1 + "";
                int value = concurrentHashMap.put(tmp, concurrentHashMap.getOrDefault(tmp, 100) - 1);
                System.out.println(finalI + " : " + value);
                latch.countDown();
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

    private void fillCacheStore() {
        for (int i = 1; i <= TOTAL_KEYS; i++) {
            concurrentHashMap.put(i + "", 100);
        }
    }

}