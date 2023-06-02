package com.example.localcachestudy.repository;

import java.util.concurrent.ConcurrentHashMap;

public class CacheStoreRepository {
    private final ConcurrentHashMap<String, Integer> store = new ConcurrentHashMap<>();

    public int find(String key) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException("스레드가 비정상적으로 종료되었습니다");
        }
        return store.get(key);
    }

    public void save(String key, int value) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException("스레드가 비정상적으로 종료되었습니다");
        }
        store.put(key, value);
    }

    public void saveWithoutSleep(String key, int value) {
        store.put(key, value);
    }
}
