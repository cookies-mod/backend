package dev.morazzer.cookies.backend.auth;

import com.sun.net.httpserver.Headers;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class BearerTokenAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    private BearerTokenAuthFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String authHeader = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)).orElse(request.getHeader(HttpHeaders.AUTHORIZATION.toLowerCase()));
        if (authHeader != null && authHeader.startsWith("Bearer ") && !authHeader.substring(7).isBlank()) {
            String accessToken = authHeader.substring(7);
            final Claims claims;
            try {
                claims = jwtUtils.parseJwtClaims(accessToken);
            } catch (Exception exception) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Authentication authentication =
                new UsernamePasswordAuthenticationToken(claims.getSubject(), null, this.mapClaims(claims));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private Collection<? extends GrantedAuthority> mapClaims(Claims claims) {
        List<GrantedAuthority> authorityList = new ArrayList<>();
        final Object scope = claims.get("scope");
        if (scope != null) {
            for (String s : scope.toString().split(";")) {
                if (s.isBlank()) {
                    continue;
                }
                authorityList.add(new SimpleGrantedAuthority(s));
            }
        }
        return authorityList;
    }
}
