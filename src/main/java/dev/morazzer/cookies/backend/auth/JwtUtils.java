package dev.morazzer.cookies.backend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    private SecretKey signingKey;
    private JwtParser jwtParser;

    private final String TOKEN_HEADER = HttpHeaders.AUTHORIZATION;
    private final String TOKEN_PREFIX = "Bearer ";

    public JwtUtils(@Value("${backend.auth.sign}") String key) {
        this.signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        this.jwtParser = Jwts.parser().verifyWith(this.signingKey).decryptWith(this.signingKey).build();
    }

    public String createToken(String modVersion, UUID uuid, String name, List<String> list) {
        return Jwts.builder()
            .expiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
            .subject(uuid.toString())
            .claim("version", modVersion)
            .claim("name", name)
            .claim("scope", String.join(";", list))
            .signWith(this.signingKey)
            .compact();
    }

    public Claims parseJwtClaims(String token) {
        return this.jwtParser.parseSignedClaims(token).getPayload();
    }

    public Claims resolveClaims(HttpServletRequest request) {
        final String token = this.resolveToken(request);
        return this.parseJwtClaims(token);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
