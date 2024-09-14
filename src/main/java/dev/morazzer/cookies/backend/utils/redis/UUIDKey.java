package dev.morazzer.cookies.backend.utils.redis;

import java.util.UUID;

public record UUIDKey(String name) implements Key<UUID> {
    @Override
    public UUID getValue(Object o) {
        return UUID.fromString(o.toString());
    }

    @Override
    public String getKey() {
        return "uuid." + name;
    }

    @Override
    public Class<UUID> getType() {
        return UUID.class;
    }
}
