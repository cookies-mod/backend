package dev.morazzer.cookies.backend.services;

import com.nimbusds.jose.shaded.gson.Gson;
import dev.morazzer.cookies.backend.entities.other.MinecraftUser;
import dev.morazzer.cookies.backend.utils.redis.UUIDKey;
import dev.morazzer.cookies.backend.utils.redis.UsernameKey;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MojangAuthService {
    private static final Gson gson = new Gson();

    private static final String BASE_URL = "https://sessionserver.mojang.com/session/minecraft/hasJoined";

    public Optional<MinecraftUser> isAuthenticated(String sharedSecret, String username, String ip) {
        String request = BASE_URL + "?username=%s&serverId=%s&ip=%s".formatted(username, sharedSecret, ip);
        try {
            final URLConnection connection = URI.create(request).toURL().openConnection();
            connection.connect();
            connection.setReadTimeout(100);
            final byte[] bytes = connection.getInputStream().readAllBytes();
            final String response = new String(bytes, StandardCharsets.UTF_8);
            connection.getInputStream().close();
            if (!response.isBlank()) {
                MinecraftUser user = gson.fromJson(response.replaceAll(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                    "$1-$2-$3-$4-$5"), MinecraftUser.class);
                MinecraftRedisService.INSTANCE.write(new UsernameKey(user.uuid()), username);
                MinecraftRedisService.INSTANCE.write(new UUIDKey(username), user.uuid());
                return Optional.of(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

}
