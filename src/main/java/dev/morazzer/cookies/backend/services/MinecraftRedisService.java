package dev.morazzer.cookies.backend.services;

import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

@Service
public class MinecraftRedisService extends AbstractRedisCache {
    public static MinecraftRedisService INSTANCE;

    private MinecraftRedisService(Cache minecraft) {
        super(minecraft);
        INSTANCE = this;
    }

}
