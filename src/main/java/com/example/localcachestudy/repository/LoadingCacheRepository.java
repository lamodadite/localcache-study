package com.example.localcachestudy.repository;

import java.util.concurrent.ConcurrentHashMap;

public class LoadingCacheRepository {
    private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();

    public String find(String key) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException("스레드가 비정상적으로 종료되었습니다");
        }
        return store.get(key);
    }

    public void save(String key, String value) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException("스레드가 비정상적으로 종료되었습니다");
        }
        store.put(key, value);
    }

    public void saveWithoutSleep(String key, String value) {
        store.put(key, value);
    }
}
