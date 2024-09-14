package dev.morazzer.cookies.backend;

import dev.morazzer.cookies.backend.auth.JwtUtils;
import dev.morazzer.cookies.entities.misc.BackendVersion;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class BackendApplication {
    public static boolean devEnvironment = false;

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean(autowireCandidate = false)
    @Profile("dev")
    public String creatAuth(JwtUtils jwtUtils) {
        final String token = jwtUtils.createToken("unknown", UUID.randomUUID(), "token", Collections.emptyList());
        System.out.println("Development token: " + token);
        return token;
    }

    @Bean(name = "isDevEnvironment")
    @Profile("dev")
    public boolean isDevelopment() {
        return true;
    }

    @Bean(name = "isDevEnvironment")
    @Profile("!dev")
    public boolean isProduction() {
        return false;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .prefixCacheNameWith(BackendVersion.CURRENT_VERSION_STRING + "_")
                .entryTtl(this::ttl))
            .initialCacheNames(Set.of("backend", "hypixel", "minecraft"))
            .build();
    }

    @Bean(name = "backend")
    public Cache backendCache(RedisCacheManager redisCacheManager) {
        return redisCacheManager.getCache("backend");
    }

    @Bean(name = "hypixel")
    public Cache hypixelCache(RedisCacheManager redisCacheManager) {
        return redisCacheManager.getCache("hypixel");
    }

    @Bean(name = "minecraft")
    public Cache minecractCache(RedisCacheManager redisCacheManager) {
        return redisCacheManager.getCache("minecraft");
    }

    private Duration ttl(Object keyValue, Object value) {
        String key = keyValue.toString();
        if (key.startsWith("uuid") || key.startsWith("username")) {
            return Duration.ofHours(12);
        }
        if (key.startsWith("player")) {
            return Duration.ofHours(24);
        }
        return Duration.ofHours(1);
    }

}
