package dev.morazzer.cookies.backend.utils.redis;

import org.springframework.cache.Cache;

public interface Key<T> {

    T getValue(Object o);
    String getKey();
    Class<T> getType();
}
