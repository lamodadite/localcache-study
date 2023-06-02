package com.example.localcachestudy.cache;

import com.example.localcachestudy.repository.LoadingCacheRepository;

import java.util.HashMap;

public class LoadingCache {
    private final HashMap<String, CacheItem> cache = new HashMap<>();
    private final HashMap<String, Thread> threads = new HashMap<>();
    private final LoadingCacheRepository repository;
    private long ttl;

    public LoadingCache(LoadingCacheRepository repository, long ttl) {
        this.repository = repository;
        this.ttl = ttl;
    }

    public String get(String key) {
        CacheItem cacheItem = cache.get(key);

        if (cacheItem == null) {
            synchronized (key.intern()) {
                String value = repository.find(key);
                cache.put(key, new CacheItem(value, ttl));
            }
        } else if (cacheItem.isExpired()) {
            if (threads.containsKey(key)) {
                return cacheItem.getItem();
            }
            Thread currentThread = Thread.currentThread();
            threads.put(key, currentThread);
            String value = repository.find(key);
            cache.put(key, new CacheItem(value, ttl));
            threads.remove(key);
        }

        return cache.get(key).getItem();
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public HashMap<String, CacheItem> getCache() {
        return cache;
    }

}
