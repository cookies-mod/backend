package dev.morazzer.cookies.backend.utils.redis;

import java.util.UUID;

public record PlayerKey(UUID uuid) implements Key<Boolean>{
    @Override
    public Boolean getValue(Object o) {
        return Boolean.valueOf(String.valueOf(o));
    }

    @Override
    public String getKey() {
        return "player." + uuid.toString();
    }

    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }
}
