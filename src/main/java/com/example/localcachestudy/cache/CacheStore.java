package com.example.localcachestudy.cache;

import com.example.localcachestudy.repository.CacheStoreRepository;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheStore {
    private final HashMap<String, AtomicInteger> cache = new HashMap<>();

    private final CacheStoreRepository repository;

    public CacheStore(CacheStoreRepository repository) {
        this.repository = repository;
    }

    public int get(String key) {
        AtomicInteger value = cache.get(key);
        if (value == null) {
            return -1; // todo 값이 없는 경우 임시로 -1처리. 추후 수정
        }
        return cache.get(key).intValue();
    }

    public int put(String key, int value) {
        if (value < 0) {
            value = 0;
        }

        int finalValue = value;
        cache.put(key, new AtomicInteger(finalValue));

        CompletableFuture.runAsync(() -> {
            repository.save(key, finalValue);
        });
        return cache.get(key).intValue();
    }
}
