package dev.morazzer.cookies.backend.services;

import com.nimbusds.jose.shaded.gson.Gson;
import dev.morazzer.cookies.backend.entities.other.MinecraftUser;
import dev.morazzer.cookies.entities.request.AuthRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.swing.text.html.Option;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MojangAuthService {
    private static final Gson gson = new Gson();

    private static final String BASE_URL = "https://sessionserver.mojang.com/session/minecraft/hasJoined";

    public Optional<MinecraftUser> isAuthenticated(String sharedSecret, String username, String ip) {
        String request = BASE_URL + "?username=%s&serverId=%s&ip=%s".formatted(username, sharedSecret, ip);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
                return Optional.ofNullable(user);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }
}
