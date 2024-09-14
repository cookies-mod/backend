package dev.morazzer.cookies.backend.services;

import dev.morazzer.cookies.backend.utils.redis.Key;
import org.springframework.cache.Cache;

public class AbstractRedisCache {

    private final Cache backend;

    protected AbstractRedisCache(Cache backend) {
        this.backend = backend;
    }

    public <T> void write(Key<T> key, T value) {
        backend.put(key.getKey(), value);
    }

    public <T> T get(Key<T> key) {
        return backend.get(key.getKey(), key.getType());
    }

    public void clear(Key<?> key) {
        backend.evict(key.getKey());
    }
}
