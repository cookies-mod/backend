package dev.morazzer.cookies.backend.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableWebSocketSecurity
public class SecurityConfig {

    BearerTokenAuthFilter bearerTokenAuthFilter;

    public SecurityConfig(BearerTokenAuthFilter bearerTokenAuthFilter) {
        this.bearerTokenAuthFilter = bearerTokenAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(request -> {
            request.requestMatchers("/login", "/auth").permitAll();
            request.anyRequest().authenticated();
        }).csrf(CsrfConfigurer::disable).addFilterBefore(bearerTokenAuthFilter, BasicAuthenticationFilter.class);
        return http.build();
    }
}
