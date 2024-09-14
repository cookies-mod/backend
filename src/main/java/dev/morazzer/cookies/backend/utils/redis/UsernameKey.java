package dev.morazzer.cookies.backend.utils.redis;

import java.util.UUID;

public record UsernameKey(UUID uuid) implements Key<String> {
    @Override
    public String getValue(Object o) {
        return o.toString();
    }

    @Override
    public String getKey() {
        return "username." + uuid;
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }
}
