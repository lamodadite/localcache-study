package com.example.localcachestudy.cache;

public class CacheItem {
    private String item;
    private long ttl;
    private long createdAt;

    public CacheItem(String item, long ttl) {
        this.item = item;
        this.ttl = ttl;
        this.createdAt = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - createdAt > ttl;
    }

    public String getItem() {
        return item;
    }
}
