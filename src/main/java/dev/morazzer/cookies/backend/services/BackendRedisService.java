package dev.morazzer.cookies.backend.services;

import dev.morazzer.cookies.backend.utils.redis.Key;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.stereotype.Service;

@Service

public class BackendRedisService extends AbstractRedisCache{

    public static BackendRedisService INSTANCE;

    private BackendRedisService(Cache backend) {
        super(backend);
        INSTANCE = this;
    }
}
